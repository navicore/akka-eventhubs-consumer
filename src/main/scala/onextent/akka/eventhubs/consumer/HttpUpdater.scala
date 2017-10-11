package onextent.akka.eventhubs.consumer

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import onextent.akka.eventhubs.consumer.models.Assessment

class HttpUpdater extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  // todo, post assessment to runkit
  def receive: PartialFunction[Any, Unit] = {
    case a: Assessment =>
      http
        .singleRequest(
          HttpRequest(
            uri = "https://navicore-test-endpoint-05gw66s9dpsj.runkit.sh"))
        .pipeTo(self)
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("got yer response, body: " + body.utf8String)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

}
