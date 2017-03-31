package com.agoda.Route

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActor.{AutoPilot, KeepRunning}
import akka.testkit.TestProbe
import com.agoda.route.ApiRouteService
import com.agoda.service.DataService
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}


class DataRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with BeforeAndAfterAll {
  implicit val formats = org.json4s.DefaultFormats
  implicit val serialization = org.json4s.jackson.Serialization

  val scoreService = TestProbe()
  val dataService = TestProbe()
  val service = new ApiRouteService(scoreService.ref, dataService.ref)

  it should "handle get hotel data" in {
    val hotels = List(1, 3, 5)

    dataService.setAutoPilot(new AutoPilot {
      override def run(sender: ActorRef, msg: Any): AutoPilot = {
        msg match {
          case DataService.GetHotelList =>
            sender ! hotels
            KeepRunning
        }
      }
    })

    Get("/api/v1/data/hotels") ~> service.route ~> check {
      entityAs[List[Int]] shouldBe hotels
      status shouldBe StatusCodes.OK
    }
  }

  it should "handle get country data" in {
    val countries = List(1, 3, 5)

    dataService.setAutoPilot(new AutoPilot {
      override def run(sender: ActorRef, msg: Any): AutoPilot = {
        msg match {
          case DataService.GetCountryList =>
            sender ! countries
            KeepRunning
        }
      }
    })

    Get("/api/v1/data/countries") ~> service.route ~> check {
      entityAs[List[Int]] shouldBe countries
      status shouldBe StatusCodes.OK
    }
  }


}