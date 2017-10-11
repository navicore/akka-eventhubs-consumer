package onextent.akka.eventhubs.consumer

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.EventHubsMessage
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.{Assessment, EhEnvelop, Log, Message}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future

object AssessmentDbSink extends LazyLogging {

  def apply(context: ActorContext): Sink[EventHubsMessage, Future[Done]] = {

    implicit val formats: DefaultFormats.type = DefaultFormats

    def createHolder(name: String): ActorRef =
      context.actorOf(AssessmentHolder.props(name), name)

    Sink.foreach[EventHubsMessage] { (m: EventHubsMessage) =>
      val body = parse(m.contentAsString).extract[EhEnvelop].contents.body
      //todo: match after the parse but before the extract
      body match { //todo: sane matching
        case s if s contains "assessment" =>
          val assessment =
            parse(body).extract[Message[Assessment]].body
          def create(): Unit = {
            val holder = createHolder(assessment.name)
            holder ! AssessmentHolder.SetAssessment(assessment)
          }
          context
            .child(assessment.name)
            .fold(create())(_ ! AssessmentHolder.SetAssessment(assessment))
        case s if s contains "log" =>
          val logmsg = parse(body).extract[Message[Log]]
          logger.info(s"log msg: $logmsg")
        case other =>
          logger.warn(s"got unknown msg type: $other")
      }
    }
  }
}
