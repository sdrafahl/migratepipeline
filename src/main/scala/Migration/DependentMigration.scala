package migration

import cats.Semigroup
import cats.FlatMap
import cats.Applicative
import cats.effect.Ref
import cats.implicits._
import cats.effect.Async
import cats.data.Kleisli

abstract class DependentMigration[F[_], A, B]:
    def up: A => F[B] 
    def down: B => F[A]
    def recovery: F[Unit]

abstract class Migration[F[_]]:
    def up: F[Unit]
    def down: F[Unit]

abstract class CombineMigration[F[_]] {
  extension [A, B, C](first: DependentMigration[F, A, B]) def ==>(second: DependentMigration[F, B, C]): DependentMigration[F, A, C]
  extension (first: Migration[F]) def ==> (second: Migration[F]): Migration[F]
  extension [A, B](dep :DependentMigration[F, A, B]) def toMigration(a: A, b: B): Migration[F]
}

object DependentMigration {
  given [F[_]](using f: Async[F]): CombineMigration[F] with {
    extension [A, B, C](first: DependentMigration[F, A, B]) def ==>(second: DependentMigration[F, B, C]): DependentMigration[F, A, C] = {
      val up0 = first.up
      val down0 = first.down
      val recovery0 = first.recovery

      val up1 = second.up
      val down1 = second.down
      val recovery1 = second.recovery

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
      new DependentMigration[F, A, C] {
        def up = newUp
        def down = newDown
        def recovery = newRecovery
      }
    }
    extension (first: Migration[F]) def ==> (second: Migration[F]): Migration[F] = {
      val newUp = first.up *> second.up
      val newDown = first.down *> second.down
      new Migration[F] {
        def up = newUp
        def down = newDown
      }
    }
    extension [A, B](dep :DependentMigration[F, A, B]) def toMigration(a: A, b: B): Migration[F] = {
      new Migration[F] {
        def up = dep.up(a).map(_ => ())
        def down = dep.down(b).map(_ => ())
      }
    }
  }      
}
