package onextent.akka.eventhubs.consumer.streams.db

import akka.actor._
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.eventhubs.consumer.streams.db.Holder._

object Holder {
  def props(name: String) = Props(new Holder(name))
  case class Set[T](state: T)
  case class Get()
}

class Holder[T](name: String) extends Actor with LazyLogging {
  def receive: Receive = hasState(None)

  def hasState(state: Option[T]): Receive = {
    case s: Set[T] =>
      context become hasState(Some(s.state))
    case Get() =>
      sender() ! state
  }
}
