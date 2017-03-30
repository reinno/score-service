package com.agoda.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.agoda.Setting
import com.agoda.service.DataService
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent.ExecutionContext


class DataRoute(dataService: ActorRef) (implicit system: ActorSystem, ec: ExecutionContext)
  extends BaseRoute {
  import Json4sSupport._

  val setting = Setting(system)
  implicit val timeout: Timeout = setting.requestTimeout

  def doRoute(implicit mat: Materializer): Route = {
    pathPrefix("data") {
      pathPrefix("hotels") {
        get {
          BaseRoute.askActorRoute[List[Int]](dataService, DataService.GetHotelList)
        }
      } ~ pathPrefix("countries") {
        get {
          BaseRoute.askActorRoute[List[Int]](dataService, DataService.GetCountryList)
        }
      }
    }
  }
}
