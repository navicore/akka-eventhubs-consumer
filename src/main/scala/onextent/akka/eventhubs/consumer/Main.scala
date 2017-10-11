package onextent.akka.eventhubs.consumer
import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.microsoft.azure.reactiveeventhubs.ResumeOnError._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.assessment.{AssessmentRoute, AssessmentService}
import onextent.akka.eventhubs.consumer.models.JsonSupport

object Main extends App with LazyLogging with JsonSupport with ErrorSupport {

  val assessmentService: ActorRef = actorSystem.actorOf(AssessmentService.props(timeout), AssessmentService.name)

  val route =
    HealthCheck ~
      AssessmentRoute(assessmentService)

  Http().bindAndHandle(route, "0.0.0.0", port)
}

