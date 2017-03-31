package com.agoda.service

import akka.actor.{Actor, ActorLogging, Props}
import com.agoda.SettingActor

object DataService {
  case object GetHotelList
  case object GetCountryList

  def props(): Props = {
    Props(new DataService())
  }
}

class DataService extends Actor with ActorLogging with SettingActor {
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
