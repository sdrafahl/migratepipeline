import cats.effect.Resource

case class State[C](id: Int, value: C)
