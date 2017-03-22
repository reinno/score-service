package com.agoda.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{as, complete, entity, pathEndOrSingleSlash, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext


object ScoreRoute {
  case class ScoreRequest(hotelId: Int, countryId: Int)
  case class ScoreResponse(hotelId: Int, score: Double)
}

class ScoreRoute()(implicit system: ActorSystem, ec: ExecutionContext)
  extends BaseRoute {
  import Json4sSupport._
  import ScoreRoute._

  def doRoute(implicit mat: Materializer): Route = {
    pathPrefix("score") {
      pathEndOrSingleSlash {
        post {
          entity(as[List[ScoreRequest]]) {
            entity =>
              val result = entity.map(req => ScoreResponse(req.hotelId, 0))
              complete(result)
          }
        }
      }
    }
  }
}
