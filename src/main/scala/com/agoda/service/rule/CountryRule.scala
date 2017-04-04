package com.agoda.service.rule

import akka.actor.{ActorRef, Props}
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.HttpRefreshWorker


object CountryRule {
  def props(next: Option[ActorRef], rule: Rule)(implicit mat: Materializer, httpClientFactory: HttpClientFactory): Props = {
    Props(new CountryRule(next, rule))
  }
}

class CountryRule(val next: Option[ActorRef], val rule: Rule)
  (implicit mat: Materializer, httpClientFactory: HttpClientFactory)
  extends RuleService {

  var specialCountries: Set[Int] = Set.empty

  override def refresh(data: RuleService.RefreshData): Unit =
    specialCountries = data.value

  override def getScore(req: ScoreRequest): Double = {
    if (specialCountries.contains(req.countryId)) {
      rule.score
    } else 0
  }

  override def getRefresh(): Unit = {
    //Future(CountryRule.Refresh(Set(1, 3, 5))) pipeTo self
    context.actorOf(Props(new HttpRefreshWorker(rule)))
  }
}


