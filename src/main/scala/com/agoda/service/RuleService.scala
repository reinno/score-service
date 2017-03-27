package com.agoda.service

import akka.actor.{Actor, ActorRef, Props, Stash}
import com.agoda.SettingActor
import com.agoda.route.ScoreRoute

object RuleService {
  case class GetScoreReq(transaction: List[(ScoreRoute.ScoreRequest, Option[Double])])
  case object Enable
  case object Disable
  case object Start

  def props(name: String, next: Option[ActorRef], enabled: Boolean): Option[Props] = {
    name match {
      case "special-country" =>
        Some(CountryRule.props(next, enabled))

      case _ =>
        None
    }
  }
}

trait RuleService extends Actor with SettingActor with Stash {
  import RuleService._

  val next: Option[ActorRef]
  val name: String
  var enabled: Boolean

  def getScore(req: ScoreRoute.ScoreRequest): Double

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
