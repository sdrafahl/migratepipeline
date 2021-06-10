package com.migration

import cats.effect.Sync
import cats.implicits._
import com.migration.MigrationResultSignal._
import com.migration.MigrationResult._

abstract class MigrationEffectOps {
  extension [A](m: Migration[A]) def corollary[F[_]](using S: Sync[F]): F[MigrationResult[A]]
}

object MigrationEffectOps {
  given MigrationEffectOps with {
    extension [A](m: Migration[A]) def corollary[F[_]](using S: Sync[F]): F[MigrationResult[A]] = {
      val downEffect = S.delay(m.down())
      S.delay(m.up()).attempt.flatMap{
        case Left(_) => downEffect.map(_ => DownMigrationRan)
        case Right(Success(a)) => S.delay(ResultSuccess(a, m.name()))
        case Right(FailedMigration) => downEffect.map(_ => DownMigrationRan)
        case Right(NoOpMigration) => S.pure(NoMigrationWasRan)
      }      
    }
  }
}
