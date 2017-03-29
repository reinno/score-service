package com.agoda.service

import akka.actor.{ActorRef, Props}
import akka.pattern.pipe
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, SECONDS}


object HotelRule {
  case class Refresh(value: Set[Int])
  case object Timeout

  def props(next: Option[ActorRef], rule: Rule): Props = {
    Props(new HotelRule(next, rule))
  }
}

class HotelRule(val next: Option[ActorRef], val rule: Rule) extends RuleService {
  import HotelRule._
  import context.dispatcher

  var specialHotels: Set[Int] = Set.empty

  def init: Receive = {
    case RuleService.Start =>
      getRefresh()
      context.system.scheduler.scheduleOnce(Duration(2, SECONDS),
        context.self, Timeout)

    case Timeout =>
      context.parent ! RuleService.StartFailed(rule.name)
      context stop self

    case Refresh(value) =>
      specialHotels = value
      context.parent ! RuleService.Started(rule.name)
      context become running

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  override def runningSpecial: Receive = {
    case Refresh(value) =>
      specialHotels = value

    case msg =>
      log.warning(s"unknown msg: $msg")
  }

  override def getScore(req: ScoreRequest): Double = {
    if (specialHotels.contains(req.hotelId)) {
      rule.score
    } else 0
  }

  override def getRefresh(): Unit = {
    Future(HotelRule.Refresh(Set(5, 7, 9))) pipeTo self
  }
}

