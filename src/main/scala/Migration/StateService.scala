package migration

abstract class StateService[F[_]] {
  def getCurrentState[A]: F[State[A]]
  def setCurrentState[A](s: State[A]): F[Unit]
}

