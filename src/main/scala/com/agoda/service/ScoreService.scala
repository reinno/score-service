package com.agoda.service

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.http.scaladsl.model.StatusCodes
import com.agoda.SettingActor
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute


object ScoreService {
  case object Start
  case class Enable(ruleName: String)
  case class Disable(ruleName: String)
  case class GetScoreRequest(requests: List[ScoreRoute.ScoreRequest])

  def props(): Props = {
    Props(new ScoreService)
  }
}

class ScoreService extends Actor with Stash with SettingActor {
  import ScoreService._

  var rulesToBeStarted: Map[String, Rule] = settings.rules
  var ruleActorHeader: Option[ActorRef] = None
  var ruleActors: Map[String, ActorRef] = Map.empty

  override def preStart() {
    self ! Start
  }

  def receive: Receive = init

  def init: Receive = {
    case Start =>
      if (rulesToBeStarted.nonEmpty) {
        val rule = rulesToBeStarted.head._2
        RuleService.props(rulesToBeStarted.head._1, None, rule.enabled) match {
          case None =>
            context become running
          case Some(props) =>
            val actor = context.actorOf(props, rule.name)
            actor ! RuleService.Start
            context become waitRuleActorStart
        }
      }

    case _ =>
      stash()
  }

  def waitRuleActorStart: Receive = {
    case Start =>
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
      switchRuleActor(msg.ruleName, true)

    case msg: Disable =>
      switchRuleActor(msg.ruleName, false)

    case _ =>
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
}
