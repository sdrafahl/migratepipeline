package com.migration

import cats.effect.Sync
import cats.implicits._
import com.migration.MigrationResultSignal._
import com.migration.MigrationEffectOps
import com.migration.MigrationEffectOps._
import com.migration.MigrationResult._

sealed trait MigrationPlayer[F[_]] {
  def runMigrationsFromLatestState[B](m: Migration[B]): F[MigrationResult[B]]
  def runMigrationsDown[B](m: Migration[B]): F[Unit]
}

object MigrationPlayer {
  def apply[F[_]: StateService: Sync](using meo: MigrationEffectOps) = {
    new MigrationPlayer[F] {
      def runMigrationsFromLatestState[B](m: Migration[B]): F[MigrationResult[B]] = {
        for {
          currentState <- summon[StateService[F]].getCurrentState
          result <- m.corollary[F]
          _ <- summon[StateService[F]].setCurrentState(m.state())
        } yield result
      }
      def runMigrationsDown[B](m: Migration[B]): F[Unit] = summon[Sync[F]].delay(m.down())      
    }
  }
}
