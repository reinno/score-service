package model

import scala.concurrent.duration.Duration

case class Rule(name: String, score: Double, refreshInterval: Duration, enabled: Boolean = true)
