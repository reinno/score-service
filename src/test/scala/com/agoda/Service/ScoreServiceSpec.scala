package com.agoda.Service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.{ActorMaterializer, Materializer}
import com.agoda.Setting
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.ScoreService
import com.agoda.util.Constants


class ScoreClient(override val countries: Set[Int] = Set(1, 3, 5),
  override val hotels: Set[Int] = Set(5, 7, 9))
  (implicit val mat: Materializer, val system: ActorSystem)
  extends HttpClientServiceHelper.RuleServiceHttpClientSenderDummy

class ScoreServiceSpec extends BaseServiceHelper.TestSpec {
  implicit val mat = ActorMaterializer()
  implicit val httpClientFactory: HttpClientFactory = () => new ScoreClient()

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
    val baseCountryRule = Map(countryRuleName -> Rule(countryRuleName, 5, endpoint = "http://127.0.0.1/api/v1/data/countries"))
    val baseHotelRule = Map(hotelRuleName -> Rule(hotelRuleName, 10, endpoint = "http://127.0.0.1/api/v1/data/hotels"))

    "return single value when single rule enabled and matched" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 5)))

      system.stop(scoreService)
    }

    "return single value when single rule enabled and not matched" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 10)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0)))

      system.stop(scoreService)
    }

    "return single value when single rule disabled and matched" in {
      val rules = Map(countryRuleName -> baseCountryRule(countryRuleName).copy(enabled = false))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(1, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(1, 0)))

      system.stop(scoreService)
    }


    "return single value when single rule enable flag switch" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

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
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.Disable("aaa")
      expectMsg(StatusCodes.NotFound)

      system.stop(scoreService)
    }

    "Enable rule not known" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.Enable("aaa")
      expectMsg(StatusCodes.NotFound)

      system.stop(scoreService)
    }


    "return max value when multi rule enabled and multi matched" in {
      val rules = baseCountryRule ++ baseHotelRule
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(5, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(5, 10)))

      system.stop(scoreService)
    }


    "return max value when multi rule enabled and multi matched with one unknown rule" in {
      val rules = baseCountryRule ++ baseHotelRule ++ Map("aaa" -> Rule("aaa", 20))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(5, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(5, 10)))

      system.stop(scoreService)
    }

    "return max value when multi rule enabled single matched" in {
      val rules = baseCountryRule ++ baseHotelRule
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(2, 1)))
      expectMsg(List(ScoreRoute.ScoreResponse(2, 5)))

      system.stop(scoreService)
    }
  }

}
