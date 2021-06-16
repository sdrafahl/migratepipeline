# Migrate Pipeline

[![Scala CI](https://github.com/sdrafahl/migratepipeline/actions/workflows/scala.yml/badge.svg)](https://github.com/sdrafahl/migratepipeline/actions/workflows/scala.yml)
[![Publish](https://github.com/sdrafahl/migratepipeline/actions/workflows/release.yml/badge.svg)](https://github.com/sdrafahl/migratepipeline/actions/workflows/release.yml)

## Usage

```
package com.migration

import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import com.migration.MigrationResultSignal._
import com.migration.MigrationOps.given

def example = {

  val currentState = State(1)

  given StateService[IO] with {
    def getCurrentState: IO[State] = IO(currentState)
    def setCurrentState(s: State): IO[Unit] = IO.unit
  }

   val migrationPlayer = MigrationPlayer[IO]  

  val migration: IO[Migration[IO, String]] = for {
    m1 <- Migration.createMigration(IO(Success("A")), IO(println("running down for A")), State(0), "Migration A")
    m2 <- Migration.createMigration(IO(Success("B")), IO(println("running down for B")), State(1), "Migration B")
    m3 <- Migration.createMigration(IO(Success("C")), IO(println("running down for C")), State(2), "Migration C")
    m1_2 = m1 ==> m2
    mi = m1_2 ==> m3
  } yield mi

  println(migration.runMigration.unsafeRunSync())
  /**
    prints --> ResultSuccess(C,Migration B ------------> Migration C)
    Skips migration A because current state is 1
   */
}


```

## Installing

```
libraryDependencies += "io.github.sdrafahl" %% "migrationpipeline" % "0.1.0"
```
