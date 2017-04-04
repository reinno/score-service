package com.agoda.service.rule

import akka.actor._
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.util.Constants

object RuleService {

  case class GetScoreReq(transaction: List[(ScoreRoute.ScoreRequest, Option[Double])])

  case object Enable

  case object Disable

  case object Start

  case object Timeout

  case class Started(name: String)

  case class StartFailed(name: String)

  case object Refresh

  case class RefreshData(value: Set[Int])

  def props(rule: Rule, next: Option[ActorRef])(implicit mat: Materializer, httpClientFactory: HttpClientFactory): Option[Props] = {
    rule.name match {
      case Constants.Rules.countryRule =>
        Some(CountryRule.props(next, rule))

      case Constants.Rules.hotelRule =>
        Some(HotelRule.props(next, rule))

      case _ =>
        None
    }
  }
}

trait RuleService extends Actor with ActorLogging with Stash {

  import RuleService._
  import context.dispatcher

  val next: Option[ActorRef]
  val rule: Rule
  var enabled: Boolean = rule.enabled

  def getScore(req: ScoreRoute.ScoreRequest): Double

  def getRefresh(): Unit

  def refresh(data: RefreshData): Unit

  context.system.scheduler.schedule(rule.refreshInterval,
    rule.refreshInterval,
    context.self, Refresh)

  def init: Receive = {
    case RuleService.Start =>
      getRefresh()
      val c: Cancellable = context.system.scheduler.scheduleOnce(Constants.Rules.initTimeout,
        context.self, Timeout)
      context become initWaitRefresh(c)

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  def initWaitRefresh(c: Cancellable): Receive = {
    case msg: RuleService.RefreshData =>
      refresh(msg)
      context.parent ! RuleService.Started(rule.name)
      c.cancel()
      unstashAll()
      context become running

    case Timeout =>
      context.parent ! RuleService.StartFailed(rule.name)
      context stop self

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  def running: Receive = {
    case req: GetScoreReq =>
      if (enabled) {
        sendToNext(GetScoreReq(req.transaction.map {
          case (scoreReq, None) =>
            (scoreReq, Some(getScore(scoreReq)))
          case (scoreReq, Some(score)) =>
            (scoreReq, Some(math.max(getScore(scoreReq), score)))
        }))
      } else {
        sendToNext(req)
      }

    case Refresh =>
      getRefresh()

    case msg: RuleService.RefreshData =>
      refresh(msg)

    case Disable =>
      enabled = false

    case Enable =>
      enabled = true

    case msg =>
      log.warning(s"unknown msg: $msg")
  }

  def receive: Receive = init

  def sendToNext(req: GetScoreReq): Unit = {
    next match {
      case Some(ruleService) =>
        ruleService.forward(req)
      case None =>
        sender() ! req.transaction.map {
          case (scoreReq, score) =>
            ScoreRoute.ScoreResponse(scoreReq.hotelId, score.getOrElse(0))
        }
    }
  }
}
