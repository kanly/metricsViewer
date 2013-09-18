package it.posteitaliane.omp.bl

import akka.actor.{Props, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import it.posteitaliane.omp.data.Metric
import it.posteitaliane.omp.bl.JSONProcessor.{Line, LineRead}

class JSONProcessor extends Actor with Logging {
  this: EventSource =>

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

  def receive = eventSourceReceiver orElse {
    case Line(line) => {
      val value: Metric = mapper.readValue(line, classOf[Metric])
      sendEvent(LineRead(value))
    }

  }

}

object JSONProcessor {

  case class Line(line: String)

  case class LineRead(line: Metric)

  def props = Props(new JSONProcessor with ProductionEventSource)

}
