package onextent.akka.eventhubs.consumer.models

import java.util.{Date, UUID}

final case class Body(body: String)

final case class EhEnvelop(contents: Body)

final case class Message[T](
    id: UUID = java.util.UUID.randomUUID(),
    kind: String = "unknown",
    datetime: Date = new Date(),
    body: T
)

// 'from' will tend to associate Log messages with the 'name' used in assessments
final case class Log(level: Int, from: String, text: String)
