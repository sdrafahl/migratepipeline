package migration

abstract class MigrationPlayer[F[_]]:
    def play[A](using stateService: StateService[F]): F[Unit]

