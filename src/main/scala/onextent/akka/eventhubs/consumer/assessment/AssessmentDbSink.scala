package onextent.akka.eventhubs.consumer.assessment

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.Holder
import org.json4s._

import scala.concurrent.Future

object AssessmentDbSink extends LazyLogging {

  def apply()(
      implicit context: ActorContext): Sink[Assessment, Future[Done]] = {

    implicit val formats: DefaultFormats.type = DefaultFormats

    Sink.foreach[Assessment] { (assessment: Assessment) =>
      def create(): ActorRef =
        context.actorOf(Holder.props(assessment.name), assessment.name)
      context
        .child(assessment.name)
        .fold(create() ! Holder.Set(assessment))(_ ! Holder.Set(assessment))
    }
  }
}
