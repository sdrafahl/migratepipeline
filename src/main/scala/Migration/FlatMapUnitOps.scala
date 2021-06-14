package com.migration

import com.migration.MigrationResultSignal._

object FlatMapUnitOps {

  private[this] def runTwoMigrations[A, B](first: Migration[A], second: Migration[B]): Migration[B] = {
    if(first.currentState().id >= second.state().id) {
      second
    } else {
      lazy val newName = () => s"${first.name()} ----> ${second.name()}"
      lazy val newUp = () => {
        first.up() match {
          case Success(_) => second.up()
          case FailedMigration(msg) => FailedMigration(msg)
          case NoOpMigration => NoOpMigration
        }
      }
      lazy val newDown = () => {
        first.down()
        second.down()
      }
      lazy val newState = second.state
      lazy val newCurrentState = second.currentState
      Migration(newName, newUp, newDown, newState, newCurrentState)
    }
  }

  extension [B](um: Migration[B]) def ==>[A](secondMigration: Migration[A]): Migration[A] = runTwoMigrations(um, secondMigration)

}

