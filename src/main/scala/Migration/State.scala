package migration

import io.circe._
import io.circe.syntax._
import io.circe.Parser
import io.circe.Encoder

abstract class State:
    def id: Int

object State {
  def apply(idOfState: Int) = new State {
    def id: Int = idOfState
  }

  given stateEncoder: Encoder[State] = new Encoder[State] {
    final def apply(a: State): Json = Json.obj(
      ("id", Json.fromInt(a.id))
    )
  }

  given stateDecoder: Decoder[State] = new Decoder[State] {
    final def apply(c: HCursor): Decoder.Result[State] =
      for {
        id <- c.downField("id").as[Int]
      } yield {
        State(id)
      }
  }
  
}
    
