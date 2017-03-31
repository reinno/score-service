package com.agoda.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.agoda.Setting
import com.agoda.service.ScoreService
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext

class RuleRoute(scoreService: ActorRef) (implicit system: ActorSystem, ec: ExecutionContext)
  extends BaseRoute {
  import Json4sSupport._

  val setting = Setting(system)
  implicit val timeout: Timeout = setting.requestTimeout

  def doRoute(implicit mat: Materializer): Route = {
    pathPrefix("rule") {
      pathPrefix(Segment) {
        ruleName => {
          path("enable") {
            post {
              BaseRoute.askActorRoute[StatusCodes](scoreService, ScoreService.Enable(ruleName))
            }
          } ~ path("disable") {
            post {
              BaseRoute.askActorRoute[StatusCodes](scoreService, ScoreService.Disable(ruleName))
            }
          }
        }
      }
    }
  }
}
