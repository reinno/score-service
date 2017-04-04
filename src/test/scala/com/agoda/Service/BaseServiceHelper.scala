package com.agoda.Service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpecLike}

object BaseServiceHelper {

  abstract class TestSpec extends TestKit(ActorSystem("MySpec"))
    with ImplicitSender with WordSpecLike with BeforeAndAfterAll with BeforeAndAfterEach with Matchers

}
