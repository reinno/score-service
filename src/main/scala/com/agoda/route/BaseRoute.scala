package com.agoda.route


import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.{HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.agoda.util.ActorUtil

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object BaseRoute {
  def askActorRoute[T](actor: ActorRef, msg: Any)
    (implicit exec: ExecutionContext, _marshaller: ToResponseMarshaller[T], timeout: Timeout): Route = {
    onComplete(ActorUtil.askActor[T](actor, msg)) {
      case Success(x) =>
        complete(x)

      case Failure(ex) =>
        failWith(ex)
    }
  }
}


trait BaseRoute {
  implicit val formats = org.json4s.DefaultFormats
  implicit val serialization = org.json4s.jackson.Serialization

  protected def doRoute(implicit mat: Materializer): Route
  protected def prefix = Slash ~ "api" / "v1"

  def route: Route = encodeResponse {
    extractMaterializer {implicit mat =>
      rawPathPrefix(prefix) {
        doRoute(mat)
      }
    }
  }
}
