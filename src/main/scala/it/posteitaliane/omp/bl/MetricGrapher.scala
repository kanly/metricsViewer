package it.posteitaliane.omp.bl

import akka.actor.{Props, Actor}
import it.posteitaliane.omp.data._
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.MetricGrapher._
import it.posteitaliane.omp.bl.MetricGrapher.Save
import it.posteitaliane.omp.data.Metric

class MetricGrapher extends Actor with Logging with MetricQueries {
  this: EventSource =>
  def receive = eventSourceReceiver orElse {
    case Save(metric) => save(metric)
    case LoadWorkstations => sender ! loadWorkstations
    case LoadMethods => sender ! loadMethods
    case LoadServices => sender ! loadServices
    case LoadErrors => sender ! loadErrors
    case LoadRequests(ws, met, ser, err) => sender ! loadRequests(ws, met, ser, err)
    case du: DataUpdated[DTO@unchecked] => sendEvent(du)
  }
}

object MetricGrapher {
  def props = Props(new MetricGrapher with ProductionEventSource)

  case class Save(metric: Metric)

  case object LoadWorkstations

  case object LoadMethods

  case object LoadServices

  case object LoadErrors

  case class DataUpdated[T <: DTO](dataType: Data, data: List[T])

  case class LoadRequests(ws: Iterable[Workstation] = Nil,
                          met: Iterable[Method] = Nil,
                          ser: Iterable[Service] = Nil,
                          err: Iterable[OmpError] = Nil)

}
