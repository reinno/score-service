package com.agoda


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.agoda.route.ApiRouteService
import com.agoda.service.ScoreService

import scala.concurrent.Await
import scala.concurrent.duration._

object Boot extends App {
  implicit val system = ActorSystem("agoda-scorer")
  implicit val timeout = Timeout(10.seconds)
  implicit val mat = ActorMaterializer()

  val setting = Setting(system)

  println(setting)

  val scoreService = system.actorOf(ScoreService.props(setting), "score-service")
  val service = new ApiRouteService(scoreService)


  val bindFuture = Http().bindAndHandle(Route.handlerFlow(service.route),
    setting.bindAddr, setting.bindPort)

  Await.result(bindFuture, 15.seconds)
  Await.result(system.whenTerminated, Duration.Inf)
}
