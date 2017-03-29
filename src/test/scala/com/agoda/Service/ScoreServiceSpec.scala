package com.agoda.Service

import akka.actor.{ActorRef, Props}
import akka.pattern.pipe
import akka.http.scaladsl.model.StatusCodes
import com.agoda.Setting
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute
import com.agoda.service.{CountryRule, ScoreService}
import com.agoda.util.Constants

import scala.concurrent.Future


object CountryRuleTestHelper {
  def props(next: Option[ActorRef], rule: Rule, countryRules: Set[Int] = Set.empty): Props = {
    Props(new CountryRuleTestHelper(next, rule, countryRules))
  }
}
class CountryRuleTestHelper(
  override val next: Option[ActorRef],
  override val rule: Rule,
  val countryRules: Set[Int])
  extends CountryRule(next, rule) {
  import context.dispatcher

  override def getRefresh(): Unit = {
    Future(CountryRule.Refresh(countryRules)) pipeTo self
  }
}

class ScoreServiceSpec extends BaseServiceHelper.TestSpec {
  "Score Service" must {
    "return single 0 when no rule" in {
      val scoreService = system.actorOf(ScoreService.props(Setting()))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 2)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0)))

      system.stop(scoreService)
    }

    "return multi 0 when no rule" in {
      val scoreService = system.actorOf(ScoreService.props(Setting()))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 2),
        ScoreRoute.ScoreRequest(3, 4)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0), ScoreRoute.ScoreResponse(3, 0)))

      system.stop(scoreService)
    }

    val countryRuleName = Constants.Rules.countryRule
    val hotelRuleName = Constants.Rules.hotelRule

    "return single value when single rule enabled and matched" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 5)))

      system.stop(scoreService)
    }

    "return single value when single rule enabled and not matched" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 10)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0)))

      system.stop(scoreService)
    }

    "return single value when single rule disabled and matched" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5, enabled = false))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0)))

      system.stop(scoreService)
    }


    "return single value when single rule enable flag switch" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 5)))

      scoreService ! ScoreService.Disable(countryRuleName)
      expectMsg(StatusCodes.OK)

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0)))

      scoreService ! ScoreService.Enable(countryRuleName)
      expectMsg(StatusCodes.OK)

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 5)))

      system.stop(scoreService)
    }

    "Disable rule not known" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.Disable("aaa")
      expectMsg(StatusCodes.NotFound)

      system.stop(scoreService)
    }

    "Enable rule not known" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.Enable("aaa")
      expectMsg(StatusCodes.NotFound)

      system.stop(scoreService)
    }


    "return max value when multi rule enabled and multi matched" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5), hotelRuleName -> Rule(hotelRuleName, 10))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(5, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(5, 10)))

      system.stop(scoreService)
    }


    "return max value when multi rule enabled and multi matched with one unknown rule" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5),
        hotelRuleName -> Rule(hotelRuleName, 10),
        "aaa" -> Rule("aaa", 20))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(5, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(5, 10)))

      system.stop(scoreService)
    }

    "return max value when multi rule enabled single matched" in {
      val rules = Map(countryRuleName -> Rule(countryRuleName, 5), hotelRuleName -> Rule(hotelRuleName, 10))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(2, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(2, 5)))

      system.stop(scoreService)
    }
  }

}
