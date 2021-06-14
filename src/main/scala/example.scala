package com.migration

import cats.effect.IO
import cats.implicits._
import java.net.URI
import cats.effect.unsafe.implicits.global
import com.migration.MigrationResultSignal._
import com.migration.MigrationEffectOps.given
import com.migration.FlatMapUnitOps._

def example = {

  val currentState = State(1)

  given StateService[IO] with {
    def getCurrentState: IO[State] = IO(currentState)
    def setCurrentState(s: State): IO[Unit] = IO.unit
  }

  val migrationPlayer = MigrationPlayer[IO]  

  val migration = for {
    m1 <- Migration.createMigration("Migration 0", () => Success(()), () => println("cleaning up migration 0"), State(0))
    m2 <- Migration.createMigration("Migration 1", () => Success(()), () => println("cleaning up migration 1"), State(1))
    m3 <- Migration.createMigration("Migration 2", () => Success(()), () => println("cleaning up migration 2"), State(2))
    migration = (m1 ==> m2 ==> m3)
    resultOfrunningTheMigration <- migrationPlayer.runMigrationsFromLatestState(migration)
  } yield resultOfrunningTheMigration

  println(migration.unsafeRunSync())

  /**    
    Skips migration 1 because the current state is 1

    Prints: 
    ResultSuccess((),Migration 1 ----> Migration 2)
    */


  val migrationValue = for {
    m1 <- Migration.createMigration("Migration 0", () => Success("a"), () => println("cleaning up migration 0"), State(0))
    m2 <- Migration.createMigration("Migration 1", () => Success("b"), () => println("cleaning up migration 1"), State(1))
    m3 <- Migration.createMigration("Migration 2", () => Success("c"), () => println("cleaning up migration 2"), State(2))
    migration = for {
      a <- m1
      b <- m2
      c <- m3
    } yield c
    resultOfrunningTheMigration <- migrationPlayer.runMigrationsFromLatestState(migration)
  } yield resultOfrunningTheMigration

  println(migrationValue.unsafeRunSync())

  /**    
    Skips all migrations in for comprehension because migration m1 is behind the current state

    Prints: 
    NoMigrationWasRan
    */
  
}
