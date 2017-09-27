package onextent.http.akka.consumer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import onextent.http.akka.consumer.models.{JsonSupport, Message}
import onextent.http.akka.consumer.routes.{ConsumerRoute, ConsumerSegmentRoute}
import scala.concurrent.ExecutionContextExecutor

object Main extends LazyLogging with JsonSupport with ErrorSupport {

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("AkkaHttpAmqpConsumer-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      HealthCheck ~
      ConsumerRoute.apply ~
      ConsumerSegmentRoute.apply

    Http().bindAndHandle(route, "0.0.0.0", port)
  }
}

