package com.agoda.route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait ApiRoute {
  implicit val actorSystem: ActorSystem
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val scoreService: ActorRef
  val dataService: ActorRef

  val scoreRoute = new ScoreRoute(scoreService)
  val ruleRoute = new RuleRoute(scoreService)
  val dataRoute = new DataRoute(dataService)

  def route: Route = scoreRoute.route ~ ruleRoute.route ~ dataRoute.route
}

class ApiRouteService(val scoreService: ActorRef, val dataService: ActorRef)
  (implicit override val actorSystem: ActorSystem) extends ApiRoute
