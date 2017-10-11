package onextent.akka.eventhubs.consumer.assessment

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.ErrorSupport
import onextent.akka.eventhubs.consumer.assessment.AssessmentService.Get
import onextent.akka.eventhubs.consumer.models.{Assessment, JsonSupport}
import spray.json._

import scala.concurrent.Future

object AssessmentRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with ErrorSupport {

  def apply(service: ActorRef): Route =
    path(urlpath / Segment) { name =>
      logRequest(urlpath) {
        handleErrors {
          cors(corsSettings) {
            get {
              val f: Future[Any] = service ask Get(name)
              onSuccess(f) { (r: Any) =>
                {
                  r match {
                    case Some(assessment: Assessment) =>
                      complete(HttpEntity(ContentTypes.`application/json`,
                                          assessment.toJson.prettyPrint))
                    case _ =>
                      complete(StatusCodes.NotFound)
                  }
                }
              }
            }
          }
        }
      }
    }
}
