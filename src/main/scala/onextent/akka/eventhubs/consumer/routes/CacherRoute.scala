package onextent.akka.eventhubs.consumer.routes

import spray.json._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.AssessmentCacher.Assessment
import onextent.akka.eventhubs.consumer.models.JsonSupport
import onextent.akka.eventhubs.consumer.{AssessmentCacher, ErrorSupport}

import scala.concurrent.Future

object CacherRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with ErrorSupport with RequestTimeout {

  implicit val system: ActorSystem = ActorSystem()
  val config: Config = ConfigFactory.load()
  lazy val assessmentCacher: ActorRef =
    system.actorOf(AssessmentCacher.props(requestTimeout(config)), AssessmentCacher.name)

  def apply: Route =
    path(urlpath / Segment) { name =>
      logRequest(urlpath) {
        handleErrors {
          cors(corsSettings) {
            get {
              logger.debug(s"get $urlpath $name")
              val q = AssessmentCacher.GetAssessment(name)
              import akka.pattern.ask
              implicit val timeout: Timeout = requestTimeout(config)
              val f: Future[Any] = assessmentCacher ask q
              // assessmentCacher ! q
              onSuccess(f) { (r: Any) => {
                r match {
                  case a: Assessment =>
                    complete(HttpEntity(ContentTypes.`application/json`, a.toJson.prettyPrint))
                  case None =>
                    complete(StatusCodes.NotFound)
                }
              } }
            }
          }
        }
      }
    }
}

trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
