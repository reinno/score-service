package com.agoda.Route

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActor.{AutoPilot, KeepRunning}
import akka.testkit.TestProbe
import akka.util.ByteString
import com.agoda.route.{ApiRouteService, ScoreRoute}
import com.agoda.service.ScoreService
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}


class ScoreRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {

  import Json4sSupport._

  implicit val formats = org.json4s.DefaultFormats
  implicit val serialization = org.json4s.jackson.Serialization

  val scoreService = TestProbe()
  val dataService = TestProbe()
  val service = new ApiRouteService(scoreService.ref, dataService.ref)

  it should "handle single request" in {

    scoreService.setAutoPilot(new AutoPilot {
      override def run(sender: ActorRef, msg: Any): AutoPilot = {
        msg match {
          case msg: ScoreService.GetScoreRequest =>
            msg.requests.size shouldBe 1
            msg.requests.head shouldBe ScoreRoute.ScoreRequest(101, 202)
            sender ! List(ScoreRoute.ScoreResponse(101, 0))
            KeepRunning
        }
      }
    })


    val jsonRequest = ByteString(
      s"""
         |[
         |    {"hotelId": 101, "countryId": 202}
         |]
        """.stripMargin)

    val postRequest = HttpRequest(
      HttpMethods.POST,
      uri = "/api/v1/score",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

    postRequest ~> service.route ~> check {

      println(response)
      entityAs[List[ScoreRoute.ScoreResponse]] shouldBe List(ScoreRoute.ScoreResponse(101, 0))
      status shouldBe StatusCodes.OK
    }
  }

  it should "handle multi requests" in {
    scoreService.setAutoPilot(new AutoPilot {
      override def run(sender: ActorRef, msg: Any): AutoPilot = {
        msg match {
          case msg: ScoreService.GetScoreRequest =>
            msg.requests.size shouldBe 2
            msg.requests.head shouldBe ScoreRoute.ScoreRequest(101, 201)
            sender ! List(ScoreRoute.ScoreResponse(101, 0), ScoreRoute.ScoreResponse(102, 0))
            KeepRunning
        }
      }
    })

    val jsonRequest = ByteString(
      s"""
         |[
         |    {"hotelId": 101, "countryId": 201},
         |    {"hotelId": 102, "countryId": 202}
         |]
        """.stripMargin)

    val postRequest = HttpRequest(
      HttpMethods.POST,
      uri = "/api/v1/score",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

    postRequest ~> service.route ~> check {
      println(response)
      entityAs[List[ScoreRoute.ScoreResponse]] shouldBe List(ScoreRoute.ScoreResponse(101, 0), ScoreRoute.ScoreResponse(102, 0))
      status shouldBe StatusCodes.OK
    }
  }

}
