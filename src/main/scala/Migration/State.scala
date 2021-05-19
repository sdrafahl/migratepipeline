package migration

abstract class State[A]:
    def id: Int
    def value: Option[A]
