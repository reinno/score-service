package com.agoda.Service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.stream.{ActorMaterializer, Materializer}
import com.agoda.Setting
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.ScoreService
import com.agoda.util.Constants

import scala.concurrent.duration._


class ScoreClient(override val countries: Set[Int] = Set(1, 3, 5),
  override val hotels: Set[Int] = Set(5, 7, 9))
  (implicit val mat: Materializer, val system: ActorSystem)
  extends HttpClientServiceHelper.RuleServiceHttpClientSenderDummy

case class RuleTestData(rule: Rule, specialId: Int, normalId: Int)

class ScoreServiceSpec extends BaseServiceHelper.TestSpec {
  implicit val mat = ActorMaterializer()
  implicit val httpClientFactory: HttpClientFactory = () => new ScoreClient()
  val countryRuleData = RuleTestData(Rule(Constants.Rules.countryRule, 5, endpoint = "http://127.0.0.1/api/v1/data/countries"), 1, 7)
  val hotelRuleData = RuleTestData(Rule(Constants.Rules.hotelRule, 10, endpoint = "http://127.0.0.1/api/v1/data/hotels"), 5, 1)
  val baseCountryRule = Map(countryRuleData.rule.name -> countryRuleData.rule)
  val baseHotelRule = Map(hotelRuleData.rule.name -> hotelRuleData.rule)

  "Score Service rule test" must {
    "return single value when single rule enabled and matched" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(
        List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(countryRuleData.specialId, countryRuleData.rule.score)))

      system.stop(scoreService)
    }

    "return single value when single rule enabled and not matched" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(
        List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.normalId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0)))

      system.stop(scoreService)
    }

    "return single value when single rule disabled and matched" in {
      val rules = Map(countryRuleData.rule.name -> baseCountryRule(countryRuleData.rule.name).copy(enabled = false))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(
        List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0)))

      system.stop(scoreService)
    }


    "return single value when single rule enable flag switch" in {
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(countryRuleData.specialId, countryRuleData.rule.score)))

      scoreService ! ScoreService.Disable(countryRuleData.rule.name)
      expectMsg(StatusCodes.OK)

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0)))

      scoreService ! ScoreService.Enable(countryRuleData.rule.name)
      expectMsg(StatusCodes.OK)

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(countryRuleData.specialId, countryRuleData.rule.score)))

      system.stop(scoreService)
    }

    "return max value when multi rule enabled and multi matched" in {
      val rules = baseCountryRule ++ baseHotelRule
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(
        List(ScoreRoute.ScoreRequest(hotelRuleData.specialId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.specialId, hotelRuleData.rule.score)))

      system.stop(scoreService)
    }


    "return max value when multi rule enabled and multi matched with one unknown rule" in {
      val rules = baseCountryRule ++ baseHotelRule ++ Map("aaa" -> Rule("aaa", 20))
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService !
        ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.specialId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.specialId, hotelRuleData.rule.score)))

      system.stop(scoreService)
    }

    "return max value when multi rule enabled single matched" in {
      val rules = baseCountryRule ++ baseHotelRule
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(
        List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, countryRuleData.rule.score)))

      system.stop(scoreService)
    }


    "return multi value when multi rule enabled and multi matched" in {
      val rules = baseCountryRule ++ baseHotelRule
      val scoreService = system.actorOf(ScoreService.props(Setting(rules = rules)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.specialId, countryRuleData.specialId)))
      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.specialId, hotelRuleData.rule.score)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, countryRuleData.rule.score)))

      system.stop(scoreService)
    }
  }

  "Score Service exception test" must {
    "return single 0 when no rule" in {
      val scoreService = system.actorOf(ScoreService.props(Setting()))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.normalId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0)))

      system.stop(scoreService)
    }

    "return multi 0 when no rule" in {
      val scoreService = system.actorOf(ScoreService.props(Setting()))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.normalId),
        ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.normalId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0), ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0)))

      system.stop(scoreService)
    }


    "Rule service first time refresh failure" in {
      class ScoreFailureClient(override val responseCode: StatusCode = StatusCodes.InternalServerError)
        extends ScoreClient

      implicit val httpClientFactory: HttpClientFactory = () => new ScoreFailureClient()

      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(Duration(7, SECONDS), List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, 0)))

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

    "receive request when init" in {
      class ScoreSlowerClient(override val httpLatency: Int = 2000) extends ScoreClient

      implicit val httpClientFactory: HttpClientFactory = () => new ScoreSlowerClient()

      val scoreService = system.actorOf(ScoreService.props(Setting(rules = baseCountryRule)))

      scoreService ! ScoreService.GetScoreRequest(List(ScoreRoute.ScoreRequest(hotelRuleData.normalId, countryRuleData.specialId)))
      expectMsg(List(ScoreRoute.ScoreResponse(hotelRuleData.normalId, countryRuleData.rule.score)))

      system.stop(scoreService)
    }
  }
}
