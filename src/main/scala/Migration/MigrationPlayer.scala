package com.migration

import cats.effect.Sync
import cats.implicits._
import com.migration.MigrationResultSignal._
import com.migration.MigrationOps
import com.migration.MigrationOps._
import cats.Monad


sealed trait MigrationPlayer[F[_]] {
  def runMigrationsFromLatestState[B](m: Migration[F, B]): F[MigrationResultSignal[B]]
  def runMigrationsDown[B](m: Migration[F, B]): F[Unit]
}

object MigrationPlayer {
  def apply[F[_]: Monad] = new MigrationPlayer[F] {
    def runMigrationsFromLatestState[B](m: Migration[F, B]): F[MigrationResultSignal[B]] = m.runUp
    def runMigrationsDown[B](m: Migration[F, B]): F[Unit] = m.down
  }
}
