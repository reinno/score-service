package com.agoda

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.agoda.model.Rule
import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.concurrent.duration.{Duration, SECONDS}

object Setting {

  def getRule(params: Config): (String, Rule) = {
    val rule = Rule (
      name = params.getString("name"),
      score = params.getDouble("score"),
      refreshInterval = Duration(params.getDuration("refreshInterval", SECONDS), SECONDS),
      enabled = params.getBoolean("enabled"),
      endpoint = params.getString("endpoint")
    )

    (rule.name, rule)
  }

  def apply(system: ActorSystem): Setting = {
    new Setting(system.settings.config.getString("agoda.server.addr"),
      system.settings.config.getInt("agoda.server.port"),
      Timeout(system.settings.config.getDuration("agoda.server.requestTimeout", SECONDS), SECONDS),
      system.settings.config.getConfigList("agoda.rules").map(getRule).toMap,
      system.settings.config.getIntList("data.countries").map(_.toInt).toList,
      system.settings.config.getIntList("data.hotels").map(_.toInt).toList
    )
  }
}

case class Setting(
  bindAddr: String = "",
  bindPort: Int = 8080,
  requestTimeout: Timeout = Timeout(10, SECONDS),
  rules: Map[String, Rule] = Map.empty,
  countryList: List[Int] = Nil,
  hotelList: List[Int] = Nil
)

trait SettingActor {
  this: Actor =>

  val settings: Setting =
    Setting(context.system)
}
