package com.agoda.service

import akka.actor.LoggingFSM
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.pipe
import akka.stream.Materializer
import com.agoda.model.Rule
import com.agoda.service.HttpClientService.HttpClientFactory
import com.agoda.service.rule.RuleService

import scala.concurrent.duration.{Duration, SECONDS}
import scala.util.Failure



object HttpRefreshWorker {
  case object Start

  sealed trait State
  object State {
    case object WaitRsp extends State
    case object WaitUnmarshalResult extends State
  }
  sealed trait Data
  object Data{
    case object None extends Data
  }
}

class HttpRefreshWorker(rule: Rule)(implicit mat: Materializer, httpClientFactory: HttpClientFactory)
  extends HttpClientService with LoggingFSM[HttpRefreshWorker.State, HttpRefreshWorker.Data] {
  import HttpRefreshWorker._
  import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
  import context.dispatcher

  implicit val httpClient = httpClientFactory()

  log.info(s"send request to ${rule.endpoint}")
  httpClient.sendHttpReq(HttpRequest(GET, uri = rule.endpoint)) pipeTo self

  startWith(State.WaitRsp, Data.None)

  when(State.WaitRsp, Duration(10, SECONDS)) {
    case Event(HttpResponse(code, _, entity, _), _) if code == StatusCodes.OK=>
      Unmarshal(entity).to[Set[Int]] pipeTo self
      goto(State.WaitUnmarshalResult)

    case Event(HttpResponse(code, _, entity, _), _)=>
      log.warning(s"http rsp failure code: $code")
      stop()

    case Event(Failure(ex), _) =>
      log.warning(s"wait http rsp failure ex: $ex")
      stop()

    case Event(StateTimeout, _)=>
      log.warning(s"wait rsp timeout")
      stop()
  }

  when(State.WaitUnmarshalResult, Duration(1, SECONDS)) {
    case Event(data: Set[Int], _)=>
      context.parent ! RuleService.RefreshData(data)
      stop()

    case Event(Failure(ex), _)=>
      log.warning(s"unmarshal failed: $ex")
      stop()

    case Event(StateTimeout, _)=>
      log.warning(s"wait unmarshal timeout")
      stop()
  }
}
