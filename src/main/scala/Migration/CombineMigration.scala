package com.migration

import cats.Semigroup
import cats.FlatMap
import cats.Applicative
import cats.effect.Ref
import cats.implicits._
import cats.effect.Async
import cats.data.Kleisli
import cats.MonadError

abstract class CombineMigration[F[_]] {
  extension [A, B](first: BracketMigration[F]) def ==> (second: BracketMigration[F]): BracketMigration[F]
}

object CombineMigration {

  given [F[_]](using f: Async[F]): CombineMigration[F] with {    
    extension [A1, B](first: BracketMigration[F]) def ==> (second: BracketMigration[F]): BracketMigration[F] = {
      new BracketMigration[F] {
        type A = (first.A, second.A)
        def name = s" ( ${first.name} -> ${second.name} ) "
        def upI = for {
          a <- first.up
          b <- second.up
        } yield (a, b)
        def downI = first.down *> second.down
      }
    }    
  }      
}
