import cats.Semigroup
import cats.FlatMap
import cats.Applicative
import cats.effect.Ref
import cats.data.Kleisli
import cats.MonadError
import cats.implicits._
import cats.effect.Async

enum MigrationCommand[F[_], A, B]:
  case DependentMigration[F[_], A, B](up: A => F[B], down: B => F[A], recovery: F[Unit]) extends MigrationCommand[F, A, B]
  case InitMigration[F[_], A, B](up: A => F[B], down: B => F[A], recovery: F[Unit]) extends MigrationCommand[F, A, B]


trait CombineMigration[F[_]] {
  def combineMigration[A, B, C](first: MigrationCommand[F, A, B], second: MigrationCommand[F, B, C]): MigrationCommand[F, A, C]
}

given [F[_]](using f: Async[F]): CombineMigration[F] with
  def combineMigration[A, B, C](first: MigrationCommand[F, A, B], second: MigrationCommand[F, B, C]): MigrationCommand[F, A, C] = (first, second) match {
    case (MigrationCommand.DependentMigration(up0, down0, recovery0), MigrationCommand.DependentMigration(up1, down1, recovery1)) => {

      val newUp: A => F[C] = (a: A) => f.defer(up0((a))).handleErrorWith{err =>
        recovery0.flatMap(_ => f.raiseError(new Throwable("Error doing migration, recovering")))
      }.flatMap{b => 
        up1(b).handleErrorWith{err =>
          down0(b).flatMap(_ => f.raiseError(new Throwable("Error doing migration, running down")))
        }
      }

      val newRecovery = recovery1 *> recovery0

      val newDown: C => F[A] = (c: C) => {
        Kleisli(down0).compose(down1).run(c).handleErrorWith{err =>
          newRecovery.flatMap(_ => f.raiseError(new Throwable("Error running down, running recovery")))
        }
      }

      MigrationCommand.DependentMigration(newUp, newDown, newRecovery)
    }
  }



extension [F[_]: CombineMigration, A, B, C](command: MigrationCommand[F, A, B])
  def ==> (command2: MigrationCommand[F, B, C]) = summon[CombineMigration[F]].combineMigration[A, B, C](command, command2)
