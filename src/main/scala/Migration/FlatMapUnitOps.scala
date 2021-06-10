package com.migration

import com.migration.MigrationResultSignal._

object FlatMapUnitOps {
  extension (um: UnitMigration) def <+>[A](secondMigration: Migration[A]): Migration[A] = {
    if(um.currentState().id > um.state().id) {
      secondMigration
    } else {
      lazy val newName = () => s"${um.name()} ----> ${secondMigration.name()}"
      lazy val newUp = () => {
        um.up() match {
          case Success(_) => secondMigration.up()
          case FailedMigration => FailedMigration
          case NoOpMigration => NoOpMigration
        }
      }
      lazy val newDown = () => {
        um.down()
        secondMigration.down()
      }
      lazy val newState = secondMigration.state
      lazy val newCurrentState = secondMigration.currentState
      Migration(newName, newUp, newDown, newState, newCurrentState)
    }
  }

}

