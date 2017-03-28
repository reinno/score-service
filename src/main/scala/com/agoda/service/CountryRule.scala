package com.agoda.service

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.pipe
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, SECONDS}




object CountryRule {
  case class Refresh(value: Set[Int])
  case object Timeout

  def props(next: Option[ActorRef], rule: Rule): Props = {
    Props(new CountryRule(next, rule))
  }
}

class CountryRule(val next: Option[ActorRef], val rule: Rule) extends RuleService {
  import CountryRule._
  import context.dispatcher

  var specialCountries: Set[Int] = Set.empty

  def init: Receive = {
    case RuleService.Start =>
      getRefresh()
      context.system.scheduler.scheduleOnce(Duration(2, SECONDS),
        context.self, Timeout)

    case Timeout =>
      context.parent ! RuleService.StartFailed(rule.name)
      context stop self

    case Refresh(value) =>
      specialCountries = value
      context.parent ! RuleService.Started(rule.name)
      context become running

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  override def runningSpecial = {
    case Refresh(value) =>
      specialCountries = value

    case msg =>
      log.warning(s"unknown msg: $msg")
  }

  override def getScore(req: ScoreRequest): Double = {
    if (specialCountries.contains(req.countryId)) {
      rule.score
    } else 0
  }

  override def getRefresh(): Unit = {
    Future(CountryRule.Refresh(Set(1, 3, 5))) pipeTo self
  }
}
