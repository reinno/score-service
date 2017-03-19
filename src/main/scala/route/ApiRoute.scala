package route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

trait ApiRoute {
  implicit val actorSystem: ActorSystem
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val statService: ActorRef
  val dataService: ActorRef

  //val assessRoute = new AssessRoute(statService, dataService)
  //val statusRoute = new StatusRoute(statService)
  //def route: Route = assessRoute.route ~ statusRoute.route
}

class ApiRouteService(val statService: ActorRef, val dataService: ActorRef)
  (implicit override val actorSystem: ActorSystem) extends ApiRoute
