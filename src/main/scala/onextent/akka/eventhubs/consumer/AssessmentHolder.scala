package onextent.akka.eventhubs.consumer

import akka.actor._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.AssessmentCacher.Assessment

object AssessmentHolder {
  def props(name: String) = Props(new AssessmentHolder(name))
  def name = "assessmentHolder"

  case class SetAssessment(assessment: Assessment)
  case class GetAssessment()
}

class AssessmentHolder (name: String) extends Actor with LazyLogging {
  import AssessmentHolder._

  var state: Option[Assessment] = None

  override def receive: PartialFunction[Any, Unit] = {
    case SetAssessment(newAssessment) =>
      logger.debug(s"SetAssessment $name")
      state = Some(newAssessment)
    case GetAssessment() =>
      logger.debug(s"GetAssessment $name")
      state match {
        case Some(a) => sender() ! a
        case _ => sender() ! None
      }
  }
}

