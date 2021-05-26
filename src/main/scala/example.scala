package com.migration

import cats.effect.IO
import cats.implicits._
import java.net.URI
import cats.effect.unsafe.implicits.global

def example = {
  val migrations = List(
    BracketMigration[IO, Unit]("Migration 0", IO(println("Running Migration 0")), IO.unit),
    BracketMigration[IO, Unit]("Migration 1", IO(println("Running Migration 1")), IO.unit),
    BracketMigration[IO, Unit]("Migration 2", IO(println("Running Migration 2")), IO.unit)
  )

  given migrationCollection: MigrationCol[IO, Map, List] = MigrationCol.fromList[IO](migrations)

  given fileState: StateService[IO] = LocalFileStateService.createLocalFileStateService[IO](new URI("teststatefile.json"))

  val player = summon[MigrationPlayer[IO]]

  player.play.unsafeRunSync()

}
