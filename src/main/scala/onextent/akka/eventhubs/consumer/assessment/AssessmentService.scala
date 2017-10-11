package onextent.akka.eventhubs.consumer.assessment

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.microsoft.azure.reactiveeventhubs.scaladsl.EventHub
import com.microsoft.azure.reactiveeventhubs.{EventHubsMessage, SourceOptions}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.assessment.AssessmentService.Get
import onextent.akka.eventhubs.consumer.models.Assessment
import onextent.akka.eventhubs.consumer.{Holder, HttpUpdateSink}

import scala.concurrent.Future

object AssessmentService {
  def props(implicit timeout: Timeout) = Props(new AssessmentService)
  def name = "assessmentService"

  final case class Get(name: String)
}

class AssessmentService(implicit timeout: Timeout)
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
    .alsoTo(HttpUpdateSink[Assessment](context, "assessment")) //todo filter w/ op
    .to(console)
    .run()

  override def receive: PartialFunction[Any, Unit] = {
    case Get(name) =>
      def notFound(): Unit = sender() ! None
      context.child(name).fold(notFound())(_ forward Holder.Get())
  }

}
