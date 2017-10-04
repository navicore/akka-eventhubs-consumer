package onextent.akka.eventhubs.consumer

import akka.Done
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.{EventHubsMessage, SourceOptions}
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.microsoft.azure.reactiveeventhubs.scaladsl.EventHub
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.JsonSupport

import scala.concurrent.Future

object Main extends App with LazyLogging with JsonSupport {

  val console: Sink[EventHubsMessage, Future[Done]] = Sink.foreach[EventHubsMessage] {
    m ⇒ println(s"enqueued-time: ${m.received}, offset: ${m.offset}, payload: ${m.contentAsString}")
  }

  EventHub().source(SourceOptions().fromSavedOffsets().saveOffsets()).to(console).run()
  // EventHub().source().to(console) .run()

}

