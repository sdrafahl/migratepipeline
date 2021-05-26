package migration

import cats.MonadError
import cats.implicits._

abstract class BracketMigration[F[_]](using me: MonadError[F, Throwable]) extends Migration[F]:
    def upI: F[A]
    def downI: F[Unit]
    def up = upI.attempt.flatMap{
      case Left(err) => downI *> me.raiseError(new Throwable(s"There was an error running migration ${name} because of error: ${err.getMessage}"))
      case Right(a) => me.pure(a)
    }
    def down = downI

object BracketMigration:
    def apply[F[_], B](n: String, u: F[B], d: F[Unit])(using me: MonadError[F, Throwable]) = new BracketMigration[F] {
      type A = B
      def name = n
      def upI: F[A] = u
      def downI: F[Unit] = d
    }
    def noOp[F[_]](using me: MonadError[F, Throwable]) = new BracketMigration[F] {
      type A = Unit
      def upI: F[Unit] = me.unit
      def downI: F[Unit] = me.unit
      def name = "No OP"
    }
    
