package com.agoda.service

import akka.actor.{Actor, ActorLogging, Props}
import com.agoda.Setting

object DataService {
  case object GetHotels
  case class SetHotels(hotels: Set[Int])
  case object GetCountries
  case class SetCountries(countries: Set[Int])

  def props(settings: Setting): Props = {
    Props(new DataService(settings))
  }
}

class DataService(settings: Setting) extends Actor with ActorLogging {
  import DataService._

  var hotels: Set[Int] = settings.hotels
  var countries: Set[Int] = settings.countries

  override def receive: Receive = {
    case GetHotels =>
      log.info(s"get hotels: $hotels")
      sender() ! hotels

    case msg: SetHotels =>
      log.info(s"set set: ${msg.hotels}")
      hotels = msg.hotels

    case GetCountries =>
      log.info(s"get countries: $countries")
      sender() ! countries

    case msg: SetCountries =>
      log.info(s"set set: ${msg.countries}")
      countries = msg.countries
  }
}
