package com.agoda.service

import akka.actor.{FSM, ActorRef, LoggingFSM, Props}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.route.ScoreRoute.ScoreRequest
import com.agoda.service.HttpClientService.HttpClientFactory

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, SECONDS}
import scala.io.Source
import scala.util.{Failure, Success}


object CountryRuleRefresher {
  case object Start

  sealed trait State
  object State {
    case object Init extends State
    case object WaitRsp extends State
    case object WaitUnmarshalResult extends State
  }
  sealed trait Data
  object Data{
    case object None extends Data
  }
}

class CountryRuleRefresher(rule: Rule)(implicit mat: Materializer, httpClientFactory: HttpClientFactory)
  extends HttpClientService with LoggingFSM[CountryRuleRefresher.State, CountryRuleRefresher.Data] {
  import CountryRuleRefresher._
  import context.dispatcher
  import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

  implicit val httpClient = httpClientFactory()

  startWith(State.Init, Data.None)

  when(State.Init, Duration(1, SECONDS)) {
    case Event(Start, _) =>
      log.info(s"send request to ${rule.endpoint}")
      httpClient.sendHttpReq(HttpRequest(POST, uri = rule.endpoint)) pipeTo self
      goto(State.WaitRsp)

    case Event(StateTimeout, _)=>
      log.warning(s"start timeout")
      stop()
  }

  when(State.WaitRsp, Duration(10, SECONDS)) {
    case Event(HttpResponse(code, _, entity, _), _) if code == StatusCodes.OK=>
      Unmarshal(entity).to[Set[Int]] pipeTo self
      goto(State.WaitUnmarshalResult)

    case Event(HttpResponse(code, _, entity, _), _)=>
      log.warning(s"http rsp failure code: $code")
      stop()

    case Event(StateTimeout, _)=>
      log.warning(s"wait rsp timeout")
      stop()
  }

  when(State.WaitUnmarshalResult, Duration(1, SECONDS)) {
    case Event(data: Set[Int], _)=>
      context.parent ! CountryRule.Refresh(data)
      goto(State.WaitUnmarshalResult)

    case Event(Failure(ex), _)=>
      log.warning(s"unmarshal failed: $ex")
      stop()

    case Event(StateTimeout, _)=>
      log.warning(s"timeout")
      stop()
  }
}


object CountryRule {
  case class Refresh(value: Set[Int])
  case object Timeout

  def props(next: Option[ActorRef], rule: Rule)(implicit mat: Materializer, httpClientFactory: HttpClientFactory): Props = {
    Props(new CountryRule(next, rule))
  }
}

class CountryRule(val next: Option[ActorRef], val rule: Rule)
  (implicit mat: Materializer, httpClientFactory: HttpClientFactory)
  extends RuleService {
  import CountryRule._
  import context.dispatcher

  var specialCountries: Set[Int] = Set.empty

  def init: Receive = {
    case RuleService.Start =>
      getRefresh()
      context.system.scheduler.scheduleOnce(Duration(10, SECONDS),
        context.self, Timeout)

    case Timeout =>
      log.warning("init timeout")
      context.parent ! RuleService.StartFailed(rule.name)
      context stop self

    case Refresh(value) =>
      specialCountries = value
      log.info(s"$value")
      context.parent ! RuleService.Started(rule.name)
      context become running

    case msg =>
      log.info(s"stash msg: $msg")
      stash()
  }

  override def runningSpecial: Receive = {
    case Refresh(value) =>
      specialCountries = value

    case msg =>
      log.warning(s"unknown msg: $msg")
  }

  override def getScore(req: ScoreRequest): Double = {
    if (specialCountries.contains(req.countryId)) {
      rule.score
    } else 0
  }

  override def getRefresh(): Unit = {
    //Future(CountryRule.Refresh(Set(1, 3, 5))) pipeTo self
    //Fresh().map(CountryRule.Refresh) pipeTo self
    val actor = context.actorOf(Props(new CountryRuleRefresher(rule)))
    actor ! CountryRuleRefresher.Start
  }


  def Fresh(): Future[Set[Int]] = {
    Future(Source.fromFile(rule.endpoint).getLines.map(s => s.toInt).toSet)
  }
}
