package com.agoda.service

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.http.scaladsl.model.StatusCodes
import com.agoda.{Setting, SettingActor}
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute


object ScoreService {
  case object Start
  case class Enable(ruleName: String)
  case class Disable(ruleName: String)
  case class GetScoreRequest(requests: List[ScoreRoute.ScoreRequest])

  def props(settings: Setting): Props = {
    Props(new ScoreService(settings))
  }
}

class ScoreService(settings: Setting) extends Actor with Stash {
  import ScoreService._

  var ruleActorHeader: Option[ActorRef] = None
  var ruleActors: Map[String, ActorRef] = Map.empty

  override def preStart() {
    self ! Start
  }

  def receive: Receive = init(settings.rules, None)

  def init(rulesToBeStarted: Map[String, Rule], lastStarted: Option[ActorRef]): Receive = {
    case Start =>
      startNewRuleService(rulesToBeStarted, None)

    case RuleService.Started(ruleName) =>
      if (ruleActorHeader.isEmpty)
        ruleActorHeader = Some(sender())
      startNewRuleService(rulesToBeStarted, Some(sender()))
      ruleActors += (ruleName -> sender())

    case RuleService.StartFailed(ruleName) =>
      startNewRuleService(rulesToBeStarted, lastStarted)

    case _ =>
      stash()
  }

  def running: Receive = {
    case GetScoreRequest(requests) =>
      ruleActorHeader match {
        case Some(actor) =>
          actor.forward(RuleService.GetScoreReq(requests.map(req => (req, None))))

        case None =>
          sender() ! requests.map(req => ScoreRoute.ScoreResponse(req.hotelId, 0))
      }

    case msg: Enable =>
      switchRuleActor(msg.ruleName, enable = true)

    case msg: Disable =>
      switchRuleActor(msg.ruleName, enable = false)
  }

  private def switchRuleActor(ruleName: String, enable: Boolean) = {
    ruleActors.get(ruleName) match {
      case Some(ruleActor) =>
        if (enable)
          ruleActor ! RuleService.Enable
        else
          ruleActor ! RuleService.Disable
        sender() ! StatusCodes.OK
      case None =>
        sender() ! StatusCodes.NotFound
    }
  }

  private def startNewRuleService(rulesToBeStarted: Map[String, Rule],
    lastStarted: Option[ActorRef]): Unit = {
    if (rulesToBeStarted.nonEmpty) {
      val rule = rulesToBeStarted.head._2

      RuleService.props(rule, lastStarted) match {
        case None =>
          startNewRuleService(rulesToBeStarted - rule.name, lastStarted)
        case Some(props) =>
          val actor = context.actorOf(props, rule.name)
          actor ! RuleService.Start
          context become init(rulesToBeStarted - rule.name, lastStarted)
      }
    } else {
      unstashAll()
      context become running
    }
  }
}
