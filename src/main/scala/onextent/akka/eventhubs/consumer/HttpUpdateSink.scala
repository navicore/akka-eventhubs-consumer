package onextent.akka.eventhubs.consumer

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.EventHubsMessage
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.{Assessment, EhEnvelop, Message}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future

object HttpUpdateSink extends LazyLogging {

  def apply(context: ActorContext): Sink[EventHubsMessage, Future[Done]] = {

    def createUpdater(name: String): ActorRef =
      context.actorOf(Props[HttpUpdater], s"${name}HttpUpdater")

    implicit val formats: DefaultFormats.type = DefaultFormats

    Sink.foreach[EventHubsMessage] { (m: EventHubsMessage) =>
      val body = parse(m.contentAsString).extract[EhEnvelop].contents.body
      //todo: match after the parse but before the extract
      body match { //todo: sane matching
        case s if s contains "assessment" =>
          val newAssessment = parse(body).extract[Message[Assessment]].body
          def create(): Unit = {
            createUpdater(newAssessment.name) ! newAssessment
          }
          context
            .child(s"${newAssessment.name}HttpUpdater")
            .fold(create())(_ ! newAssessment)
        case other =>
          logger.warn(s"got unknown msg type: $other")
      }
    }
  }
}

