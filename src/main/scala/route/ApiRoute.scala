package route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait ApiRoute {
  implicit val actorSystem: ActorSystem
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val scoreRoute = new ScoreRoute()
  def route: Route = scoreRoute.route
}

class ApiRouteService()
  (implicit override val actorSystem: ActorSystem) extends ApiRoute
