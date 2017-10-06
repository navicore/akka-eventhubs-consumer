package onextent.akka.eventhubs.consumer

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models._
import onextent.akka.eventhubs.consumer.routes.CacherRoute

object Main extends App with LazyLogging with JsonSupport with ErrorSupport {

  val route =
    HealthCheck ~
      CacherRoute.apply

  Http().bindAndHandle(route, "0.0.0.0", port)

}
