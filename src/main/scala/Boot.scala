
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import route.ApiRouteService

import scala.concurrent.Await
import scala.concurrent.duration._

object Boot extends App {
  implicit val system = ActorSystem("agoda-scorer")
  implicit val timeout = Timeout(10.seconds)
  implicit val mat = ActorMaterializer()

  val setting = Setting(system)

  println(setting)

  val service = new ApiRouteService()

  val bindFuture = Http().bindAndHandle(Route.handlerFlow(service.route),
    setting.bindAddr, setting.bindPort)

  Await.result(bindFuture, 15.seconds)
  Await.result(system.whenTerminated, Duration.Inf)
}
