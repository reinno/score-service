package com.agoda.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext

object RuleRoute {
  case class Enable(name: String)
  case class Disable(name: String)
}
class RuleRoute (implicit system: ActorSystem, ec: ExecutionContext)
  extends BaseRoute {
  import Json4sSupport._
  import RuleRoute._

  def doRoute(implicit mat: Materializer): Route = {
    pathPrefix("rule") {
      path("enable") {
        post {
          entity(as[Enable]) {
            entity =>
              complete(StatusCodes.OK)
          }
        }
      } ~ path("disable") {
        entity(as[Disable]) {
          entity =>
            complete(StatusCodes.OK)
        }
      }
    }
  }
}
