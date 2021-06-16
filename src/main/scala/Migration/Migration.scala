package com.migration

import com.migration.MigrationResultSignal._
import cats.Applicative
import cats.Monad
import cats.ApplicativeError
import cats.MonadError
import cats.Show
import cats.implicits._

abstract class Migration[F[_], B] {
  def runUp: F[Mig[F, B]] 
}

case class Mig[F[_], B](name: String, up: MigrationResultSignal[B], state: State, currentState: State, down: F[Unit])

object Migration {
  def apply[F[_],B](na: F[Mig[F, B]]) = new Migration[F, B] {
    def runUp: F[Mig[F, B]] = na
  }
 
  def map[F[_]: Applicative, B, C](f: B => C)(migration: Migration[F, B]): Migration[F, C] = {
    val newUp = migration.runUp.map(m => Mig(m.name, m.up.map(f), m.state, m.currentState, m.down))
    Migration(newUp)
  }  

  def flatten[F[_]: Monad, B](m: Migration[F, Migration[F, B]]): Migration[F, B] = {
    val newUp: F[Mig[F, B]] = m.runUp.flatMap {firstMigration => 
      firstMigration.up match {
        case Success(mi: Migration[F, B]) => {
          mi.runUp.map {secondMigration =>
            val newLabel = s"${firstMigration.name} ---------> ${secondMigration.name}"
            val newDown = for {
              a <- firstMigration.down
              b <- secondMigration.down
            } yield b
            Mig[F, B](newLabel, secondMigration.up, secondMigration.state, secondMigration.currentState, newDown)
          }
        }
        case FailedMigration(err) => summon[Monad[F]].pure(Mig[F, B](s"${firstMigration.name} ---------> ${err}", FailedMigration(err), firstMigration.state, firstMigration.currentState, firstMigration.down))
        case NoOpMigration        => summon[Monad[F]].pure(Mig[F, B](s"${firstMigration.name} ---------> no-operation", NoOpMigration, firstMigration.state, firstMigration.currentState, firstMigration.down))
      }
    }
    Migration(newUp)
  }

  def flatMap[F[_]: Monad, B, C](f: B => Migration[F, C])(m: Migration[F, B]): Migration[F, C] = flatten(map(f)(m))
}
