package com.agoda.Route

import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import akka.util.ByteString
import com.agoda.Setting
import com.agoda.route.ApiRouteService
import com.agoda.service.DataService
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}


class DataRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {
  implicit val formats = org.json4s.DefaultFormats
  implicit val serialization = org.json4s.jackson.Serialization

  val countries = Set(1, 3, 5)
  val hotels = Set(2, 4, 6)

  it should "handle get hotel data" in {
    val scoreService = TestProbe()
    val dataService = system.actorOf(DataService.props(
      Setting(countries = countries, hotels = hotels)))
    val service = new ApiRouteService(scoreService.ref, dataService)

    Get("/api/v1/data/hotels") ~> service.route ~> check {
      entityAs[Set[Int]] shouldBe hotels
      status shouldBe StatusCodes.OK
    }
  }

  it should "handle get country data" in {
    val scoreService = TestProbe()
    val dataService = system.actorOf(DataService.props(
      Setting(countries = countries, hotels = hotels)))
    val service = new ApiRouteService(scoreService.ref, dataService)

    Get("/api/v1/data/countries") ~> service.route ~> check {
      entityAs[Set[Int]] shouldBe countries
      status shouldBe StatusCodes.OK
    }
  }


  it should "handle set hotel data" in {
    val scoreService = TestProbe()
    val dataService = system.actorOf(DataService.props(
      Setting(countries = countries, hotels = hotels)))
    val service = new ApiRouteService(scoreService.ref, dataService)

    val newhotels = Set(7, 8, 9)

    val jsonRequest = ByteString("[7, 8, 9]")

    val postRequest = HttpRequest(
      HttpMethods.POST,
      uri = "/api/v1/data/hotels",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

    postRequest ~> service.route ~> check {
      status shouldBe StatusCodes.OK
    }

    Get("/api/v1/data/hotels") ~> service.route ~> check {
      entityAs[Set[Int]] shouldBe newhotels
      status shouldBe StatusCodes.OK
    }
  }

  it should "handle set country data" in {
    val scoreService = TestProbe()
    val dataService = system.actorOf(DataService.props(
      Setting(countries = countries, hotels = hotels)))
    val service = new ApiRouteService(scoreService.ref, dataService)

    val newCountries = Set(7, 8, 9)

    val jsonRequest = ByteString("[7, 8, 9]")

    val postRequest = HttpRequest(
      HttpMethods.POST,
      uri = "/api/v1/data/countries",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

    postRequest ~> service.route ~> check {
      status shouldBe StatusCodes.OK
    }

    Get("/api/v1/data/countries") ~> service.route ~> check {
      entityAs[Set[Int]] shouldBe newCountries
      status shouldBe StatusCodes.OK
    }
  }


}