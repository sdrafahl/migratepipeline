package com.migration

import cats.implicits._
import cats.Applicative

abstract class MigrationCol[F[_], M[_, _], G[_]] {
  def migrations: M[Int, BracketMigration[F]]
  def getNewMigrations(current: State): G[BracketMigration[F]]  
}


object MigrationCol {
  def apply[F[_]: Applicative](m: Map[Int, BracketMigration[F]]) = new MigrationCol[F, Map, List] {
    def migrations: Map[Int, BracketMigration[F]] = m
    def getNewMigrations(current: State): List[BracketMigration[F]] = {     
      m.view.slice(current.id, m.size - 1).toList.map(a => a._2)
    }
  }

  def fromList[F[_]: Applicative](l: List[BracketMigration[F]]) = MigrationCol(Map.from(l.mapWithIndex((a, b) => (b, a))))
}

    
