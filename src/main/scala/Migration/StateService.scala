package migration

import java.net.URI
import cats.effect.Async
import cats.implicits._
import io.circe._
import io.circe.syntax._
import io.circe.Parser
import io.circe.Encoder
import java.io.File
import java.io.PrintWriter
import cats.effect.Resource

abstract class StateService[F[_]] {
  def getCurrentState: F[State]
  def setCurrentState(s: State): F[Unit]
}

object StateService:
    def apply[F[_]: StateService] = summon[StateService[F]]

object LocalFileStateService {
  def createLocalFileStateService[F[_]](file: URI)(using async: Async[F], stateDecoder: Decoder[State], stateEncoder: Encoder[State]) = new StateService[F] {
    def getCurrentState: F[State] = {
      for {
        contentsOfFile <- async.delay(scala.io.Source.fromFile(file).mkString)
        statePgm <- Encoder.encodeString(contentsOfFile).as[State] match {
          case Left(err) => async.raiseError[State](new Throwable(s"There was an error getting state ${err.getMessage}"))
          case Right(a) => async.pure[State](a)
        }        
      } yield statePgm
    }
    def setCurrentState(s: State): F[Unit] = {
      val contentsToWrite = s.asJson.toString
      val writer = async.delay(new PrintWriter(new File(file)))
      val writerResource: Resource[F, PrintWriter] = Resource.make(writer)(wr => async.delay(wr.close))
      writerResource.use(a => async.delay(a.write(contentsToWrite)))      
    }
  }
}
