package onextent.akka.eventhubs.consumer

import AssessmentHolder._
import akka.actor._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.models.Assessment

object AssessmentHolder {
  def props(name: String) = Props(new AssessmentHolder(name))
  def name = "assessmentHolder"
  case class SetAssessment(assessment: Assessment)
  case class GetAssessment()
}

class AssessmentHolder(name: String) extends Actor with LazyLogging {
  def receive: Receive = hasState(None)

  def createHttpUpdater(name: String): ActorRef =
    context.actorOf(Props[HttpUpdater], s"${name}HttpUpdater")

  def hasState(state: Option[Assessment]): Receive = {
    case SetAssessment(newAssessment) =>
      context become hasState(Some(newAssessment))
      def create(): Unit = {
        val updater = createHttpUpdater(newAssessment.name)
        updater ! newAssessment
      }
      context
        .child(s"${newAssessment.name}HttpUpdater")
        .fold(create())(_ ! newAssessment)

    case GetAssessment() =>
      sender() ! state
  }
}
