package com.agoda.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{as, entity, pathEndOrSingleSlash, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.agoda.Setting
import com.agoda.service.ScoreService
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext


object ScoreRoute {

  case class ScoreRequest(hotelId: Int, countryId: Int)

  case class ScoreResponse(hotelId: Int, score: Double)

}

class ScoreRoute(scoreService: ActorRef)(implicit system: ActorSystem, ec: ExecutionContext)
  extends BaseRoute {

  import Json4sSupport._
  import ScoreRoute._

  val setting = Setting(system)
  implicit val timeout: Timeout = setting.requestTimeout

  def doRoute(implicit mat: Materializer): Route = {
    pathPrefix("score") {
      pathEndOrSingleSlash {
        post {
          entity(as[List[ScoreRequest]]) {
            entity =>
              BaseRoute.askActorRoute[List[ScoreResponse]](scoreService, ScoreService.GetScoreRequest(entity))
          }
        }
      }
    }
  }
}
