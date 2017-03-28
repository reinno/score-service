package com.agoda.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext

trait ApiRoute {
  implicit val actorSystem: ActorSystem
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val scoreService: ActorRef

  val scoreRoute = new ScoreRoute(scoreService)
  val ruleRoute = new RuleRoute(scoreService)
  def route: Route = scoreRoute.route ~ ruleRoute.route
}

class ApiRouteService(val scoreService: ActorRef)
  (implicit override val actorSystem: ActorSystem) extends ApiRoute
