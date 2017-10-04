package onextent.akka.eventhubs.consumer.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.ErrorSupport
import onextent.akka.eventhubs.consumer.models.JsonSupport

object CacherRoute
    extends JsonSupport
    with LazyLogging
    with Directives
    with ErrorSupport {

  def apply: Route =
    path(urlpath) {
      logRequest(urlpath) {
        handleErrors {
          cors(corsSettings) {
            get {
              logger.debug(s"get $urlpath")
                logger.info("ejs 1")
                complete( HttpEntity(ContentTypes.`application/json`, s"ejs") )
            }
          }
        }
      }
    }
}
