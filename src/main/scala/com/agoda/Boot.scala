package com.agoda


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.agoda.route.ApiRouteService
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.{DataService, HttpClientSingle, ScoreService}

import scala.concurrent.Await
import scala.concurrent.duration._

object Boot extends App {
  implicit val system = ActorSystem("agoda-scorer")
  implicit val timeout = Timeout(10.seconds)
  implicit val mat = ActorMaterializer()
  implicit val httpClientFactory: HttpClientFactory = () => new HttpClientSingle()

  val settings = Setting(system)

  println(settings)

  val scoreService = system.actorOf(ScoreService.props(settings), "score-service")
  val dataService = system.actorOf(DataService.props(settings), "data-service")
  val service = new ApiRouteService(scoreService, dataService)


  val bindFuture = Http().bindAndHandle(Route.handlerFlow(service.route),
    settings.bindAddr, settings.bindPort)

  Await.result(bindFuture, 15.seconds)
  Await.result(system.whenTerminated, Duration.Inf)
}
