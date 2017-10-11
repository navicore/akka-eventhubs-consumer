package onextent.akka.eventhubs.consumer

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.microsoft.azure.reactiveeventhubs.scaladsl.EventHub
import com.microsoft.azure.reactiveeventhubs.{EventHubsMessage, SourceOptions}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.AssessmentCacher._

import scala.concurrent.Future

object AssessmentCacher {
  def props(implicit timeout: Timeout) = Props(new AssessmentCacher)
  def name = "assessmentCacher"

  case class GetAssessment(name: String)
}

class AssessmentCacher(implicit timeout: Timeout)
    extends Actor
    with LazyLogging {

  val console: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { m =>
      logger.debug(
        s"enqueued-time: ${m.received}, offset: ${m.offset}, payload: ${m.contentAsString}")
    }

  EventHub()
    .source(SourceOptions().fromSavedOffsets().saveOffsets())
    .alsoTo(AssessmentDbSink(context))
    .alsoTo(HttpUpdateSink(context))
    .to(console)
    .run()

  override def receive: PartialFunction[Any, Unit] = {
    case GetAssessment(aname) =>
      def notFound(): Unit = sender() ! None
      def askForAssessment(child: ActorRef): Unit =
        child forward AssessmentHolder.GetAssessment()
      context.child(aname).fold(notFound())(askForAssessment)
  }

}
