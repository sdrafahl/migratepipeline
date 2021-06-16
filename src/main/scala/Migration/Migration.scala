package com.migration

import com.migration.MigrationResultSignal._
import cats.Applicative
import cats.Monad
import cats.ApplicativeError
import cats.MonadError
import cats.Show
import cats.implicits._

abstract class Migration[F[_], B] {
  def runUp: F[MigrationResultSignal[B]]
  def down: F[Unit]
  def state: State
  def currentState: State
  def label: String
}

object Migration {
  def apply[F[_],B](na: F[MigrationResultSignal[B]], d: F[Unit], s: State, cState: State, lab: String) = new Migration[F, B] {
    def runUp: F[MigrationResultSignal[B]] = na
    def down = d
    def state = s
    def currentState = cState
    def label = lab
  }
 
  def map[F[_]: Applicative, B, C](f: B => C)(migration: Migration[F, B]): Migration[F, C] = {
    val newUp = migration.runUp.map(m => m.map(f))
    Migration(newUp, migration.down, migration.state, migration.currentState, migration.label)
  }    
}
