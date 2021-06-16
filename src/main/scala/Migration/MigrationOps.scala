package com.migration

import cats.MonadError
import com.migration.MigrationResultSignal._
import cats.implicits._

abstract class MigrationOps[F[_]] {
  extension [A](firstMigration: Migration[F, A]) def ==>[C](secondMigration: Migration[F, C]): Migration[F, C]
  extension [A](firstMigration: Migration[F, A]) def -->[C](secondMigration: Migration[F, C]): Migration[F, (A, C)]
}

object MigrationOps {
  given migrationOps[F[_]](using me: MonadError[F, Throwable]): MigrationOps[F] with {
    extension [A](firstMigration: Migration[F, A]) def ==>[C](secondMigration: Migration[F, C]): Migration[F, C] = {
      if(firstMigration.currentState.id >= secondMigration.state.id) {
        secondMigration
      } else {
        val newUp = firstMigration.runUp.attempt.flatMap{
          case Left(err) => {
            for {
              _ <- firstMigration.down
              r <- me.pure(FailedMigration(err.getMessage))
            } yield r            
          }
          case Right(Success(a)) => secondMigration.runUp
          case Right(FailedMigration(err)) => {
            for {
              _ <- firstMigration.down
              r <- me.pure(FailedMigration(err))
            } yield r            
          }
          case Right(NoOpMigration) => me.pure(NoOpMigration)
        }
        val newDown = for {
          _ <- firstMigration.down
          _ <- secondMigration.down
        } yield ()
        Migration(newUp, newDown, secondMigration.state, secondMigration.currentState, s"${firstMigration.label} ------------> ${secondMigration.label}")
      }
    }
    extension [A](firstMigration: Migration[F, A]) def -->[C](secondMigration: Migration[F, C]): Migration[F, (A, C)] = {
      if(firstMigration.currentState.id >= secondMigration.state.id) {
        Migration(me.pure(NoOpMigration), firstMigration.down, firstMigration.state, firstMigration.currentState, s"${firstMigration.label} ------------> no op")
      } else {
        val newUp = firstMigration.runUp.attempt.flatMap{
          case Left(err) => {
            for {
              _ <- firstMigration.down
              r <- me.pure(FailedMigration(err.getMessage))
            } yield r            
          }
          case Right(Success(a)) => secondMigration.runUp.map(_.map(bc => (a, bc)))
          case Right(FailedMigration(err)) => {
            for {
              _ <- firstMigration.down
              r <- me.pure(FailedMigration(err))
            } yield r            
          }
          case Right(NoOpMigration) => me.pure(NoOpMigration)
        }
        val newDown = for {
          _ <- firstMigration.down
          _ <- secondMigration.down
        } yield ()
        Migration(newUp, newDown, secondMigration.state, secondMigration.currentState, s"${firstMigration.label} ------------> ${secondMigration.label}")
      }
    }
  }
}
