package com.agoda.util

import scala.concurrent.duration.{Duration, SECONDS}

object Constants {
  object Rules {
    val countryRule = "special-country"
    val hotelRule = "special-hotel"
    val initTimeout = Duration(6, SECONDS)
  }

  val httpTimeout = Duration(5, SECONDS)
  val unmarshalTimeout = Duration(1, SECONDS)
}
