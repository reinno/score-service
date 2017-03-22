package com.agoda.service

import akka.actor.{Actor, Stash}
import akka.actor.Actor.Receive
import com.agoda.route.ScoreRoute
import com.agoda.service.ScoreService.GetScoreRequest


object ScoreService {
  case object Start
  case class GetScoreRequest(requests: List[ScoreRoute.ScoreRequest])
}

class ScoreService extends Actor with Stash {
  import ScoreService._

  override def preStart() {
    self ! Start
  }

  def receive: Receive = {
    case GetScoreRequest(requests) =>

    case _ =>
  }

  def init: Receive = {
    case Start =>

    case msg =>
      stash()
  }
}
