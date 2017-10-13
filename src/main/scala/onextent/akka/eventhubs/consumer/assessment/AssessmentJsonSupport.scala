package onextent.akka.eventhubs.consumer.assessment

import onextent.akka.eventhubs.consumer.models.{JsonSupport, Message}
import spray.json._

trait AssessmentJsonSupport extends JsonSupport {

  implicit val assessmentFormat: RootJsonFormat[Assessment] = jsonFormat2(
    Assessment)
  implicit val assessmentMessageFormat: RootJsonFormat[Message[Assessment]] =
    jsonFormat4(Message[Assessment])
}
