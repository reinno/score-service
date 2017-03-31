package com.agoda.service

import akka.actor.ActorSystem
import akka.http.scaladsl.{HttpsConnectionContext, Http}
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.Materializer
import org.json4s.{jackson, DefaultFormats}

import scala.concurrent.Future


trait HttpClientService {
  implicit val formats = DefaultFormats
  implicit val serialization = jackson.Serialization

  implicit val httpClient: HttpClientSender
}

trait HttpClientSender {
  def sendHttpReq(req: HttpRequest): Future[HttpResponse]
}

class HttpClientSingle(implicit val system: ActorSystem, val mat: Materializer) extends HttpClientSender {
  def sendHttpReq(req: HttpRequest): Future[HttpResponse] =
    Http().singleRequest(req)
}

object HttpClientService {
  type HttpClientFactory = () => HttpClientSender
}
