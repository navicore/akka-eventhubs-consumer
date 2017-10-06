package onextent.akka.eventhubs.consumer

import akka.actor._
import onextent.akka.eventhubs.consumer.AssessmentCacher.Assessment

object AssessmentHolder {
  def props(name: String) = Props(new AssessmentHolder(name))
  def name = "assessmentHolder"

  case class SetAssessment(assessment: Assessment)
  case class GetAssessment()
}

class AssessmentHolder (name: String) extends Actor {
  import AssessmentHolder._

  var assessment: Option[Assessment] = None

  override def receive: PartialFunction[Any, Unit] = {
    case SetAssessment(newAssessment) =>
      assessment = Some(newAssessment)
    case GetAssessment() =>
      assessment match {
        case Some(_) => sender() ! assessment
        case _ => sender() ! None
      }
  }
}

