package com.agoda.model

import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}

case class Rule(name: String,
  score: Double,
  refreshInterval: FiniteDuration = Duration(5, SECONDS),
  enabled: Boolean = true,
  endpoint: String = "")
