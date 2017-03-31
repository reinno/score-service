package com.agoda.service.rule

import akka.actor.{ActorRef, Cancellable, Props}
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.HttpRefreshWorker

import scala.concurrent.duration.{Duration, SECONDS}





object CountryRule {

  case object Timeout

  def props(next: Option[ActorRef], rule: Rule)(implicit mat: Materializer, httpClientFactory: HttpClientFactory): Props = {
    Props(new CountryRule(next, rule))
  }
}

class CountryRule(val next: Option[ActorRef], val rule: Rule)
  (implicit mat: Materializer, httpClientFactory: HttpClientFactory)
  extends RuleService {
  import CountryRule._
  import context.dispatcher

  var specialCountries: Set[Int] = Set.empty

  def init: Receive = {
    case RuleService.Start =>
      getRefresh()
      val c: Cancellable = context.system.scheduler.scheduleOnce(Duration(2, SECONDS),
        context.self, Timeout)
      context become initWaitRefresh(c)


    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  def initWaitRefresh(c: Cancellable): Receive = {
    case Timeout =>
      context.parent ! RuleService.StartFailed(rule.name)
      context stop self

    case RuleService.RefreshData(value) =>
      specialCountries = value
      context.parent ! RuleService.Started(rule.name)
      c.cancel()
      unstashAll()
      context become running

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  override def runningSpecial: Receive = {
    case RuleService.RefreshData(value) =>
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
    //Future(CountryRule.Refresh(Set(1, 3, 5))) pipeTo self
    context.actorOf(Props(new HttpRefreshWorker(rule)))
  }
}


