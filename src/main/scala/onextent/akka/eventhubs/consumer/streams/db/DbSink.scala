package onextent.akka.eventhubs.consumer.streams.db

import akka.Done
import akka.actor._
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging
import org.json4s._

import scala.concurrent.Future

object DbSink extends LazyLogging {

  def apply[T](
      implicit context: ActorContext): Sink[(String, T), Future[Done]] = {

    implicit val formats: DefaultFormats.type = DefaultFormats

    Sink.foreach[(String, T)] {
      case (name, item) =>
        def create(): ActorRef =
          context.actorOf(Holder.props(name), name)
        context
          .child(name)
          .fold(create() ! Holder.Set(item))(_ ! Holder.Set(item))
    }
  }
}
