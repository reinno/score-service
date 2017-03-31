package com.agoda.service

import akka.actor.{Actor, ActorLogging, Props}
import com.agoda.{Setting, SettingActor}

object DataService {
  case object GetHotelList
  case object GetCountryList

  def props(settings: Setting): Props = {
    Props(new DataService(settings))
  }
}

class DataService(settings: Setting) extends Actor with ActorLogging {
  import DataService._

  override def receive: Receive = {
    case GetHotelList =>
      log.info(s"get hotels: ${settings.hotelList}")
      sender() ! settings.hotelList

    case GetCountryList =>
      log.info(s"get countries: ${settings.countryList}")
      sender() ! settings.countryList
  }
}
