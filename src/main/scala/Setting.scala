import akka.actor.ActorSystem
import com.typesafe.config.Config
import model.Rule

import scala.collection.JavaConversions._
import scala.concurrent.duration.{Duration, SECONDS}

object Setting {

  def getRule(params: Config): (String, Rule) = {
    val rule = Rule (
      name = params.getString("name"),
      score = params.getDouble("score"),
      refreshInterval = Duration(params.getDuration("refreshInterval", SECONDS), SECONDS),
      enabled = params.getBoolean("enabled")
    )

    (rule.name, rule)
  }

  def apply(system: ActorSystem): Setting = {
    new Setting(system.settings.config.getString("agoda.server.addr"),
      system.settings.config.getInt("agoda.server.port"),
      system.settings.config.getConfigList("agoda.rules").map(getRule).toMap
    )
  }
}

case class Setting(
  bindAddr: String,
  bindPort: Int,
  rules: Map[String, Rule]
)
