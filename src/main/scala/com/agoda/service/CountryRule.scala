package com.agoda.service

import akka.actor.{ActorRef, Props}
import com.agoda.route.ScoreRoute.ScoreRequest

object CountryRule {
  def props(next: Option[ActorRef], enabled: Boolean): Props = {
    Props(new CountryRule(next, enabled))
  }
}
class CountryRule(val next: Option[ActorRef], var enabled: Boolean) extends RuleService {
  val name: String = "special-country"
  var specialCountries: Set[Int] = Set.empty

  def init: Receive = {
    case RuleService.Start =>
      context become running
    case msg =>
      stash()
  }

  override def getScore(req: ScoreRequest): Double = {
    if (specialCountries.contains(req.countryId)) {
      settings.rules.get(name) match {
        case Some(rule) =>
          rule.score
        case None =>
          0
      }
    } else {
      0
    }
  }
}
