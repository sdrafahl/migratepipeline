
trait StateService[F[_], A] {
  def getCurrentState: F[State[A]]
  def setCurrentState(s: State[A]): F[Unit]
}
