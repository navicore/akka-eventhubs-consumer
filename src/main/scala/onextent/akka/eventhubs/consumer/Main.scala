package onextent.akka.eventhubs.consumer

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.microsoft.azure.reactiveeventhubs.scaladsl._
import com.microsoft.azure.reactiveeventhubs.{EventHubsMessage, SourceOptions}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models._
import onextent.akka.eventhubs.consumer.routes.CacherRoute
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future

object Main extends App with LazyLogging with JsonSupport with ErrorSupport {

  val console: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { m ⇒
      println(
        s"enqueued-time: ${m.received}, offset: ${m.offset}, payload: ${m.contentAsString}")
    }

  val updateDbActors: Sink[EventHubsMessage, Future[Done]] =
    Sink.foreach[EventHubsMessage] { (m: EventHubsMessage) ⇒
      implicit val formats: DefaultFormats.type = DefaultFormats
      val ehEnvelope = parse(m.contentAsString).extract[EhEnvelop]
      ehEnvelope.contents.body match {
        case s if s contains "assessment"  =>
          val assessment = parse(ehEnvelope.contents.body).extract[Message[Assessment]]
          println(s"ejs got assessment: $assessment")
        case s if s contains "log"  => //todo: sane matching
          val log = parse(ehEnvelope.contents.body).extract[Message[Log]]
          println(s"ejs got log: $log")
        case other =>
          println(s"ejs got other: $other")
      }
    }

  EventHub()
    .source(SourceOptions().fromSavedOffsets().saveOffsets())
    .alsoTo(updateDbActors)
    .to(console)
    .run()

  val route =
    HealthCheck ~
      CacherRoute.apply

  Http().bindAndHandle(route, "0.0.0.0", port)
}
