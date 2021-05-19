package migration

abstract class MigrationCol[F[_]] {
  def migrations: Map[Int, Migration[F]]
  def getNewMigrations[A](current: State[A]): List[Migration[F]]  
}

    
