trait MigrationCommandEvaluator[F[_]] {
  def evaluate[A, B](command: MigrationCommand[F, A, B]): F[B]
}

