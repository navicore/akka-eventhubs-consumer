package onextent.http.akka.consumer.routes

import java.util.Date

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import onextent.http.akka.consumer.ErrorSupport
import onextent.http.akka.consumer.models.{JsonSupport, Message}
import spray.json._

object ConsumerSegmentRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with ErrorSupport {

  def apply: Route =
    path(urlpath / Segment) { name =>
      logRequest(s"$urlpath / $name") {
        handleErrors {
          cors(corsSettings) {
            get {
              val response =
                Message(java.util.UUID.randomUUID(), new Date(), s"hiya $name")
              complete(response.toJson.prettyPrint)
            } ~
              post {
                decodeRequest {
                  entity(as[Message]) { m =>
                    val response = Message(java.util.UUID.randomUUID(),
                                           new Date(),
                                           s"${m.body} to you, too!")
                    complete(response.toJson.prettyPrint)
                  }
                }
              }
          }
        }
      }
    }
}
