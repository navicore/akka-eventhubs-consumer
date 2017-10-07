package onextent.akka.eventhubs.consumer.routes

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.{Assessment, JsonSupport}
import onextent.akka.eventhubs.consumer.{AssessmentCacher, ErrorSupport}
import AssessmentCacher._
import spray.json._

import scala.concurrent.Future

object CacherRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with ErrorSupport
    with RequestTimeout {

  implicit val system: ActorSystem = ActorSystem()
  val config: Config = ConfigFactory.load()

  implicit val timeout: Timeout = requestTimeout(config)

  val assessmentCacher: ActorRef =
    system.actorOf(AssessmentCacher.props(timeout), AssessmentCacher.name)

  def apply: Route =
    path(urlpath / Segment) { name =>
      logRequest(urlpath) {
        handleErrors {
          cors(corsSettings) {
            get {
              val f: Future[Any] = assessmentCacher ask GetAssessment(name)
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

trait RequestTimeout {
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
