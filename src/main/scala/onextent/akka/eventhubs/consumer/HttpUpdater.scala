package onextent.akka.eventhubs.consumer

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString

class HttpUpdater extends Actor with ActorLogging {

  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive: PartialFunction[Any, Unit] = {

    case HttpResponse(StatusCodes.OK, _, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("got yer response, body: " + body.utf8String)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()

    case obj: Any =>
      http
        .singleRequest(
          HttpRequest(
            method = HttpMethods.POST,
            uri = "https://navicore-test-endpoint-05gw66s9dpsj.runkit.sh",
            entity = HttpEntity(contentType = `application/json`, obj.toString)
          )
        )
        .pipeTo(self)
  }

}
