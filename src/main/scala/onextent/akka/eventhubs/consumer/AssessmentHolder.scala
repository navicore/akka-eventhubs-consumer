package onextent.akka.eventhubs.consumer

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
  import AssessmentHolder._
  def receive: Receive = hasState(None)

  def hasState(state: Option[Assessment]): Receive = {
    case SetAssessment(newAssessment) =>
      context become hasState(Some(newAssessment))
    case GetAssessment() =>
      sender() ! state
  }
}
