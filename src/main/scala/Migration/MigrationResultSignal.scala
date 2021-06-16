package com.migration

import cats.Applicative
import cats.Monad

enum MigrationResultSignal[+A]:
    case Success(v: A)
    case FailedMigration(msg: String)
    case NoOpMigration 
   

object MigrationResultSignal {
  given applicative[A]: Applicative[MigrationResultSignal] with {
    override def ap[A, B](ff: MigrationResultSignal[A => B])(fa: MigrationResultSignal[A]): MigrationResultSignal[B] = {
      (ff, fa) match {
        case (Success(f), Success(v)) => Success(f(v))
        case (_, FailedMigration(ms)) => FailedMigration(ms)
        case (_, NoOpMigration)       => NoOpMigration
        case _                        => NoOpMigration
      }
    }
    override def pure[A](a: A): MigrationResultSignal[A] = Success(a)
    override def map[A, B](fa: MigrationResultSignal[A])(f: A => B): MigrationResultSignal[B] = ap(pure(f))(fa)
  }

  private[this] def flatten[A](m: MigrationResultSignal[MigrationResultSignal[A]]): MigrationResultSignal[A] = {
    m match {
      case Success(a)           => a
      case FailedMigration(msg) => FailedMigration(msg)
      case NoOpMigration        => NoOpMigration
    }
  }

  given monad(using app: Applicative[MigrationResultSignal]): Monad[MigrationResultSignal] with {
    override def pure[A](a: A): MigrationResultSignal[A] = app.pure(a)

    override def flatMap[A, B](fa: MigrationResultSignal[A])(f: A => MigrationResultSignal[B]): MigrationResultSignal[B] = MigrationResultSignal.flatten(app.map(fa)(f))

    override def tailRecM[A, B](a: A)(f: A => MigrationResultSignal[Either[A, B]]): MigrationResultSignal[B] = {
      f(a) match {
        case Success(Right(b)) => Success(b)
        case NoOpMigration => NoOpMigration
        case FailedMigration(msg) => FailedMigration(msg)
        case Success(Left(a)) => tailRecM(a)(f)
      }
    }
  }
}
