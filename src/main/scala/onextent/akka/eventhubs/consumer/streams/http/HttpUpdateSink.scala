package onextent.akka.eventhubs.consumer.streams.http

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.EventHubsMessage
import com.typesafe.scalalogging.LazyLogging
import org.json4s._

import scala.concurrent.Future

object HttpUpdateSink extends LazyLogging {

  def apply[T: Manifest](containsPattern: String)(
      implicit context: ActorContext
  ): Sink[EventHubsMessage, Future[Done]] = {

    def createUpdater(): ActorRef =
      context.actorOf(Props[HttpUpdater], "HttpUpdater")

    implicit val formats: DefaultFormats.type = DefaultFormats

    Sink.foreach[EventHubsMessage] { (m: EventHubsMessage) =>
      val body = m.contentAsString
      //todo: match after the parse but before the extract
      body match { //todo: sane matching
        case json if json contains containsPattern =>
          def create(): Unit = {
            createUpdater() ! json
          }
          context
            .child("HttpUpdater")
            .fold(create())(_ ! json)
        case other =>
          logger.warn(s"got unknown msg type: $other")
      }
    }
  }
}
