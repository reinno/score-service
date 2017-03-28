package com.agoda.Route

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActor.{AutoPilot, KeepRunning}
import akka.testkit.TestProbe
import com.agoda.route.ApiRouteService
import com.agoda.service.ScoreService
import com.agoda.util.Constants
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}


class RuleRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {
  implicit val formats = org.json4s.DefaultFormats
  implicit val serialization = org.json4s.jackson.Serialization

  val scoreService = TestProbe()
  val service = new ApiRouteService(scoreService.ref)

  it should "handle enable rule" in {
    for (status <- List(StatusCodes.OK, StatusCodes.NotFound)) {
      scoreService.setAutoPilot(new AutoPilot {
        override def run(sender: ActorRef, msg: Any): AutoPilot = {
          msg match {
            case ScoreService.Enable(rule) =>
              rule shouldBe Constants.Rules.countryRule
              sender ! status
              KeepRunning
          }
        }
      })

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = s"/api/v1/rule/${Constants.Rules.countryRule}/enable")

      postRequest ~> service.route ~> check {
        status shouldBe status
      }
    }
  }


  it should "handle disable rule success" in {
    for (status <- List(StatusCodes.OK, StatusCodes.NotFound)) {
      scoreService.setAutoPilot(new AutoPilot {
        override def run(sender: ActorRef, msg: Any): AutoPilot = {
          msg match {
            case ScoreService.Disable(rule) =>
              rule shouldBe Constants.Rules.countryRule
              sender ! status
              KeepRunning
          }
        }
      })

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = s"/api/v1/rule/${Constants.Rules.countryRule}/disable")

      postRequest ~> service.route ~> check {
        status shouldBe status
      }
    }
  }
}