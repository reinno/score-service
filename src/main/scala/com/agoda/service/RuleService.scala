package com.agoda.service

import akka.actor._
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute
import com.agoda.util.Constants

object RuleService {
  case class GetScoreReq(transaction: List[(ScoreRoute.ScoreRequest, Option[Double])])
  case object Enable
  case object Disable
  case object Start
  case class Started(name: String)
  case class StartFailed(name: String)
  case object Refresh

  def props(rule: Rule, next: Option[ActorRef]): Option[Props] = {
    rule.name match {
      case Constants.Rules.countryRule =>
        Some(CountryRule.props(next, rule))

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

  context.system.scheduler.schedule(rule.refreshInterval,
    rule.refreshInterval,
    context.self, Refresh)

  def init: Receive

  def running: Receive = runningCommon orElse runningSpecial
  def runningSpecial: Receive = Actor.emptyBehavior
  def runningCommon: Receive = {
    case req: GetScoreReq =>
      if (enabled) {
        sendToNext(GetScoreReq(req.transaction.map{
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

    case Disable =>
      enabled = false

    case Enable =>
      enabled = true
  }

  def receive: Receive = init

  def sendToNext(req: GetScoreReq): Unit = {
    next match {
      case Some(ruleService) =>
        ruleService.forward(req)
      case None =>
        sender() ! req.transaction.map{
          case (scoreReq, score) =>
            ScoreRoute.ScoreResponse(scoreReq.hotelId, score.getOrElse(0))}
    }
  }
}
