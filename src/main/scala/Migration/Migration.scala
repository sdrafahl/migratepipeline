import cats.Semigroup
import cats.FlatMap
import cats.Applicative
import cats.effect.Ref
import cats.implicits._

sealed trait Migration[A]

// type ContextMigration[F[_], A] = F[Migration[A]]

// trait InitialMigration[A] extends Migration[A] {
//   def up(a: => A): State[A]
//   def down(b: A => Unit): State[Unit]
// }

// trait DependentMigration[A, B] extends Migration[A] {
//   def up(a: A => B): State[B]
//   def down(a: B => A): State[A]
// }

// object Migration {

//   given migrationSemiGroup[A](using grouping: Semigroup[A]): Semigroup[Migration[A]] with {
//     def combine(x: Migration[A], y: Migration[A]) = (x, y) match {
//       case (x: )
//     }
//   }

//   def apply[A](x: A, y: A => Unit) = {
//       new InitialMigration[A] {
//         def up: State[A] = State(1, x)
//         def down(b: A => Unit): State[Unit] = State(0, ())
//     }    
//   }
// }

// def combine(x: Migration[F], y: Migration[F]): Migration[F] = ???


