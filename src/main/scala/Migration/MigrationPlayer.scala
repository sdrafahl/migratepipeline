package migration

import cats.MonadError
import cats.Reducible
import cats.Traverse
import cats.implicits._

abstract class MigrationPlayer[F[_]]:
    def play[A, M[_, _], G[_]](using stateService: StateService[F], migrationColection: MigrationCol[F, M, G], combineMigrations: CombineMigration[F], re: Traverse[G]): F[Unit]    

object MigrationPlayer {
  given [F[_]](using me: MonadError[F, Throwable]): MigrationPlayer[F] with {
    def play[A, M[_, _], G[_]](using stateService: StateService[F], migrationColection: MigrationCol[F, M, G], combineMigrations: CombineMigration[F], re: Traverse[G]): F[Unit] = {
      for {
        currentState <- stateService.getCurrentState
        migrationsToRun = migrationColection.getNewMigrations(currentState)
        migrationToRun = migrationsToRun.reduceLeftOption((a, b) => a ==> b).getOrElse(BracketMigration.noOp)
        res <- migrationToRun.up
        newState = State((currentState.id + migrationsToRun.size).toInt)
        _ <- stateService.setCurrentState(newState)
      } yield me.unit
    }    
  }
}
