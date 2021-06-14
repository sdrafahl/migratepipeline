package com.migration

import com.migration.MigrationResultSignal._
import cats.Applicative
import cats.Monad
import cats.ApplicativeError
import cats.implicits._

sealed abstract class Migration[B] {
  def name: () => String
  def up: () => MigrationResultSignal[B]
  def down: () => Unit
  def state:() => State
  def currentState: () => State
}

type UnitMigration = Migration[Unit] 

object Migration {
  def apply[B](n: () => String, u: () => MigrationResultSignal[B], d: () => Unit, migrationState :() => State, cState: () => State) = new Migration[B] {
    def name = n
    def up: () => MigrationResultSignal[B] = () => {
        u() match {
          case Success(b) => Success(b)
          case NoOpMigration => NoOpMigration
          case FailedMigration(msg) => {
            d()
            FailedMigration(msg)
          }
        }
    }
    def down: () => Unit = d
    def state = migrationState
    def currentState = cState
  }

  def createMigration[F[_]: StateService: Monad, B](label: String, up: () => MigrationResultSignal[B], down: () => Unit, state: State) = for {
    currentState <- summon[StateService[F]].getCurrentState
    m = Migration(() => label, up, down, () => state, () => currentState)
  } yield m

  given applicative[A]: Applicative[Migration] with {
    override def ap[A, B](ff: Migration[A => B])(fa: Migration[A]): Migration[B] = {
      if(fa.currentState().id > fa.state().id || ff.currentState().id > ff.state().id) {
        Migration(fa.name, () => NoOpMigration, () => (), fa.state, fa.currentState)
      } else {
        val newUp: () => MigrationResultSignal[B] = () => {
        val result = fa.up()
        result match {
          case FailedMigration(msg) => FailedMigration(msg)
          case NoOpMigration => NoOpMigration
          case Success(a) => ff.up() match {
            case FailedMigration(msg) => FailedMigration(msg)
            case NoOpMigration => NoOpMigration
            case Success(af) => Success(af(a))
          }
        }
      }
        val newDown = () => {
          fa.down()
          ff.down()
        }
        val newName = () => s"${fa.name()} -----> ${ff.name()}"
        Migration(newName, newUp, newDown, ff.state, ff.currentState)
      }      
    }
    override def pure[A](a: A): Migration[A] = Migration(() => s"(pure)", () => Success(a), () => (), () => State(0), () => State(0))
    override def map[A, B](fa: Migration[A])(f: A => B): Migration[B] = ap(pure(f))(fa)
  }

  private[this] def flatten[A](x: Migration[Migration[A]]): Migration[A] = {

    lazy val secondMigration = x.up()

    lazy val newUp: () => MigrationResultSignal[A] = () => {
      secondMigration match {
        case NoOpMigration => NoOpMigration
        case FailedMigration(msg) => FailedMigration(msg)
        case Success(a) => a.up()
      }
    }

    lazy val newDown: () => Unit = () => {
      x.down
      secondMigration match {
        case FailedMigration(_) => ()
        case NoOpMigration => ()
        case Success(a) => a.down
      }
    }

    lazy val newName = () =>  secondMigration match {
      case FailedMigration(_) => "Failed Migration"
      case NoOpMigration => "No Op"
      case Success(a) => s"${x.name()} ----> ${a.name()}"
    }

    lazy val newState = () => {
      secondMigration match {
        case FailedMigration(_) => x.state()
        case NoOpMigration => x.state()
        case Success(a) => State(Math.max(x.state().id, a.state().id))
      }
    }

    Migration(newName, newUp, newDown, newState, x.currentState)
  }

  given migrationMonad(using app: Applicative[Migration]): Monad[Migration] with {
    override def pure[A](a: A): Migration[A] = app.pure(a)
    override def flatMap[A, B](fa: Migration[A])(f: A => Migration[B]): Migration[B] = Migration.flatten(app.map(fa)(f)) 
    override def tailRecM[A, B](a: A)(f: A => Migration[Either[A, B]]): Migration[B] = {
      lazy val migration = f(a)
      if(migration.state().id > migration.currentState().id) {
        Migration(migration.name, () => NoOpMigration, migration.down, migration.state, migration.currentState)
      } else {
        migration.up() match {
          case FailedMigration(msg) => Migration(migration.name,() => FailedMigration(msg), () => (), migration.state, migration.currentState)
          case NoOpMigration => Migration(migration.name,() => NoOpMigration, () => (), migration.state, migration.currentState)
          case Success(Left(l)) => tailRecM(a)(f)
          case Success(Right(r)) => Migration(() => s"${migration.name()}", () => Success(r), migration.down, migration.state, migration.currentState)          
        }
      }
    }
  }
}
