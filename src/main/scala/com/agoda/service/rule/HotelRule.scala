package com.agoda.service.rule

import akka.actor.{ActorRef, Props}
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.HttpRefreshWorker


object HotelRule {
  def props(next: Option[ActorRef], rule: Rule)
    (implicit mat: Materializer, httpClientFactory: HttpClientFactory): Props = {
    Props(new HotelRule(next, rule))
  }
}

class HotelRule(val next: Option[ActorRef], val rule: Rule)
  (implicit mat: Materializer, httpClientFactory: HttpClientFactory) extends RuleService {

  var specialHotels: Set[Int] = Set.empty

  override def refresh(data: RuleService.RefreshData): Unit =
    specialHotels = data.value

  override def getScore(req: ScoreRequest): Double = {
    if (specialHotels.contains(req.hotelId)) {
      rule.score
    } else 0
  }

  override def getRefresh(): Unit = {
    context.actorOf(Props(new HttpRefreshWorker(rule)))
  }
}


