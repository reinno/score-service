package util

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

object ActorUtil {
  def askActor[T](actor: ActorRef, msg: Any)(implicit ex: ExecutionContext, timeout: Timeout): Future[T] = {
    (actor ? msg).asInstanceOf[Future[T]]
  }
}
