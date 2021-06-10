# Migrate Pipeline

[![Scala CI](https://github.com/sdrafahl/migratepipeline/actions/workflows/scala.yml/badge.svg)](https://github.com/sdrafahl/migratepipeline/actions/workflows/scala.yml)
[![Publish](https://github.com/sdrafahl/migratepipeline/actions/workflows/release.yml/badge.svg)](https://github.com/sdrafahl/migratepipeline/actions/workflows/release.yml)

## Usage

```
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

  val migration: Migration[String] = for {
    migration1 <- Migration(() => "Migration 0", () => Success("a"), () => println("cleaning up migration 0"), () => State(0),() => currentState)
    migration2 <- Migration(() => "Migration 1", () => Success("c"), () => println("cleaning up migration 1"), () => State(1),() => currentState)
    migration3 <- Migration(() => "Migration 2", () => Success("d"), () => println("cleaning up migration 2"), () => State(2),() => currentState)
  } yield migration3

  val m4: UnitMigration = Migration(() => "Migration 0", () => Success(()), () => println("cleaning up migration 0"), () => State(0),() => currentState) <+>
  Migration(() => "Migration 1", () => Success(()), () => println("cleaning up migration 1"), () => State(1),() => currentState) <+>
  Migration(() => "Migration 2", () => Success(()), () => println("cleaning up migration 2"), () => State(2),() => currentState)

  println(m4.corollary[IO].unsafeRunSync())

  /**
    Prints
    Skips migrations
    ResultSuccess((),Migration 1 ----> Migration 2)
    */

  val migrationAsEffect: IO[MigrationResult[String]] = migration.corollary[IO]

  println(migrationAsEffect.unsafeRunSync())

  /**
    Prints 
   ResultSuccess(d,Migration 0 -----> (pure) ----> (pure) ----> Migration 1 -----> (pure) ----> (pure) ----> Migration 2 -----> (pure) ----> (pure) ----> (pure))
    */
  
}


```

## Installing

```
libraryDependencies += "io.github.sdrafahl" %% "migrationpipeline" % "0.0.2"
```
