package com.agoda.Service

import com.agoda.Setting
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute
import com.agoda.service.ScoreService
import com.agoda.util.Constants

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

    "return single value when single rule enabled" in {
      val rules = Map(Constants.Rules.countryRule -> Rule(Constants.Rules.countryRule, 5))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 5)))

      system.stop(scoreService)
    }
  }

}
