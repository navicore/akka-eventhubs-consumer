package onextent.akka.eventhubs.consumer

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.microsoft.azure.reactiveeventhubs.scaladsl._
import com.microsoft.azure.reactiveeventhubs.{EventHubsMessage, SourceOptions}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.JsonSupport
import onextent.akka.eventhubs.consumer.routes.CacherRoute

import scala.concurrent.Future

object Main extends App with LazyLogging with JsonSupport with ErrorSupport {

  val console: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { m ⇒
      println(
        s"enqueued-time: ${m.received}, offset: ${m.offset}, payload: ${m.contentAsString}")
    }

  val console2: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { m ⇒
      println(
        s"ejs console 2 enqueued-time: ${m.received}, offset: ${m.offset}, payload: ${m.contentAsString}")
    }

  EventHub()
    .source(SourceOptions().fromSavedOffsets().saveOffsets())
    .alsoTo(console2)
    .to(console)
    .run()

  val route =
    HealthCheck ~
      CacherRoute.apply

  Http().bindAndHandle(route, "0.0.0.0", port)
}
