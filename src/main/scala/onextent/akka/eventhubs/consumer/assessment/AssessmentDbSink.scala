package onextent.akka.eventhubs.consumer.assessment

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.EventHubsMessage
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.Holder
import onextent.akka.eventhubs.consumer.models.{Assessment, EhEnvelop, Message}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future

object AssessmentDbSink extends LazyLogging {

  def apply(context: ActorContext): Sink[EventHubsMessage, Future[Done]] = {

    implicit val formats: DefaultFormats.type = DefaultFormats

    Sink.foreach[EventHubsMessage] { (m: EventHubsMessage) =>
      val body = parse(m.contentAsString).extract[EhEnvelop].contents.body
      //todo: match after the parse but before the extract
      body match { //todo: sane matching
        case s if s contains "assessment" =>
          val assessment =
            parse(body).extract[Message[Assessment]].body
          def create(): ActorRef =
            context.actorOf(Holder.props(assessment.name), assessment.name)
          context
            .child(assessment.name)
            .fold(create() ! Holder.Set(assessment))(_ ! Holder.Set(assessment))
        case other =>
          logger.warn(s"got unknown msg type: $other")
      }
    }
  }
}
