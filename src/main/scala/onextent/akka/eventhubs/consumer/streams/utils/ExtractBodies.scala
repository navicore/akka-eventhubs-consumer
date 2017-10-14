package onextent.akka.eventhubs.consumer.streams.utils

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.microsoft.azure.reactiveeventhubs.EventHubsMessage
import org.json4s.DefaultFormats

object ExtractBodies {

  implicit val formats: DefaultFormats.type = DefaultFormats
  def apply(contains: String): Flow[EventHubsMessage, String, NotUsed] =
    Flow[EventHubsMessage]
      .map(e => e.contentAsString)
      .filter(_.contains(contains))
}
