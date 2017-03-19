package Route

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import route.ApiRouteService


class ScoreRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {

  val service = new ApiRouteService()

  it should "handle single request" in {
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
      status shouldBe StatusCodes.OK
    }
  }

  it should "handle multi requests" in {
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
      status shouldBe StatusCodes.OK
    }
  }

}
