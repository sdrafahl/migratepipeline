package migration

import cats.MonadError

abstract class Migration[F[_]]:
    type A
    def name: String
    def up: F[A]
    def down: F[Unit]


object Migration {
  def apply[F[_], B](n: String, u: F[B], d: F[Unit]) = new Migration[F] {
    type A = B
    def name: String = n
    def up: F[A] = u
    def down: F[Unit] = d
  }
}
