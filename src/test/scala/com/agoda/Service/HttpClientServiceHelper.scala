package com.agoda.Service

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.agoda.service.HttpClientSender
import org.scalatest.Matchers

import scala.concurrent.Future

object HttpClientServiceHelper {


  trait HttpClientSenderDummy extends HttpClientSender with Matchers {
    import system.dispatcher

    implicit val formats = org.json4s.DefaultFormats
    implicit val serialization = org.json4s.jackson.Serialization
    implicit val system: ActorSystem
    implicit val mat: Materializer

    def sendPartial: PartialFunction[HttpRequest, Future[HttpResponse]] = {
      case x =>
        Future(HttpResponse(StatusCodes.InternalServerError))
    }

    def sendHttpReq(req: HttpRequest): Future[HttpResponse] = {
      sendPartial(req)
    }
  }

  trait RuleServiceHttpClientSenderDummy extends HttpClientSenderDummy {
    self: HttpClientSenderDummy =>
    import system.dispatcher
    import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

    val responseCode = StatusCodes.OK
    val countries: Set[Int]
    val hotels: Set[Int]

    def localSender: PartialFunction[HttpRequest, Future[HttpResponse]] = {
      case x if x.uri.path.startsWith(Uri.Path("/api/v1/data/countries")) =>
        val f = Marshal(countries).to[ResponseEntity]
        f.map(x => HttpResponse(status = responseCode, entity = x.withContentType(ContentTypes.`application/json`)))

      case x if x.uri.path.startsWith(Uri.Path("/api/v1/data/hotels")) =>
        val f = Marshal(hotels).to[ResponseEntity]
        f.map(x => HttpResponse(status = responseCode, entity = x.withContentType(ContentTypes.`application/json`)))
    }

    override def sendPartial: PartialFunction[HttpRequest, Future[HttpResponse]] = {
      localSender.orElse(super.sendPartial)
    }
  }
}
