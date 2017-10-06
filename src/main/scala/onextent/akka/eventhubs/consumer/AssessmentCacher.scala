package onextent.akka.eventhubs.consumer

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.microsoft.azure.reactiveeventhubs.scaladsl.EventHub
import com.microsoft.azure.reactiveeventhubs.{EventHubsMessage, SourceOptions}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.{EhEnvelop, Log, Message}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, _}

import scala.concurrent.Future

object AssessmentCacher {
  def props(implicit timeout: Timeout) = Props(new AssessmentCacher)
  def name = "assessmentCacher"

  case class Assessment(name: String, value: Double)
  case class GetAssessment(name: String)
}

class AssessmentCacher(implicit timeout: Timeout)
    extends Actor
    with LazyLogging {
  import AssessmentCacher._

  val console: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { m ⇒
      logger.debug(
        s"enqueued-time: ${m.received}, offset: ${m.offset}, payload: ${m.contentAsString}")
    }

  def createAssessmentHolder(name: String): ActorRef =
    context.actorOf(AssessmentHolder.props(name), name)

  val updateDbActors: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { (m: EventHubsMessage) ⇒
      implicit val formats: DefaultFormats.type = DefaultFormats
      val ehEnvelope = parse(m.contentAsString).extract[EhEnvelop]
      //todo: match after the parse but before the extract
      ehEnvelope.contents.body match { //todo: sane matching
        case s if s contains "assessment" =>
          val assessment =
            parse(ehEnvelope.contents.body).extract[Message[Assessment]].body
          def create(): Unit = {
            val holder = createAssessmentHolder(assessment.name)
            holder ! AssessmentHolder.SetAssessment(assessment)
          }
          context
            .child(assessment.name)
            .fold(create())(holder =>
              holder ! AssessmentHolder.SetAssessment(assessment))
        case s if s contains "log" =>
          val log = parse(ehEnvelope.contents.body).extract[Message[Log]]
          logger.info(s"got log msg: $log")
        case other =>
          logger.warn(s"got unknown msg type: $other")
      }
    }

  EventHub()
    .source(SourceOptions().fromSavedOffsets().saveOffsets())
    .alsoTo(updateDbActors)
    .to(console)
    .run()

  override def receive: PartialFunction[Any, Unit] = {
    case GetAssessment(aname) =>
      logger.debug(s"GetAssessment $aname")
      def notFound(): Unit = sender() ! None
      def askForAssessment(child: ActorRef): Unit = child forward AssessmentHolder.GetAssessment()
      context.child(aname).fold(notFound())(askForAssessment)
  }

}
