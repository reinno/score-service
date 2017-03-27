package com.agoda.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait ApiRoute {
  implicit val actorSystem: ActorSystem
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val scoreService: ActorRef

  val scoreRoute = new ScoreRoute()
  def route: Route = scoreRoute.route
}

class ApiRouteService(val scoreService: ActorRef)
  (implicit override val actorSystem: ActorSystem) extends ApiRoute
