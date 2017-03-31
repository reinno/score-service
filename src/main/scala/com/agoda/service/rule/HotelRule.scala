package com.agoda.service.rule

import akka.actor.{ActorRef, Cancellable, Props}
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.HttpRefreshWorker

import scala.concurrent.duration.{Duration, SECONDS}


object HotelRule {
  case object Timeout

  def props(next: Option[ActorRef], rule: Rule)
    (implicit mat: Materializer, httpClientFactory: HttpClientFactory): Props = {
    Props(new HotelRule(next, rule))
  }
}

class HotelRule(val next: Option[ActorRef], val rule: Rule)
  (implicit mat: Materializer, httpClientFactory: HttpClientFactory) extends RuleService {
  import HotelRule._
  import context.dispatcher

  var specialHotels: Set[Int] = Set.empty

  def init: Receive = {
    case RuleService.Start =>
      getRefresh()
      val timer: Cancellable = context.system.scheduler.scheduleOnce(Duration(2, SECONDS),
        context.self, Timeout)
      context become initWaitRefresh(timer)


    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  def initWaitRefresh(timer: Cancellable): Receive = {
    case RuleService.RefreshData(value) =>
      specialHotels = value
      context.parent ! RuleService.Started(rule.name)
      timer.cancel()
      unstashAll()
      context become running

    case Timeout =>
      context.parent ! RuleService.StartFailed(rule.name)
      context stop self

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  override def runningSpecial: Receive = {
    case RuleService.RefreshData(value) =>
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
    context.actorOf(Props(new HttpRefreshWorker(rule)))
  }
}


