package it.posteitaliane.omp.bl

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Props, ActorRef, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.MetricGrapher._
import it.posteitaliane.omp.Metrics
import it.posteitaliane.omp.Metrics.GiveMeUI

import akka.pattern.{ask, pipe}
import scala.concurrent.Await
import scala.concurrent.duration._
import it.posteitaliane.omp.data._
import it.posteitaliane.omp.bl.ProductionEventSource.RegisterListener
import it.posteitaliane.omp.bl.MetricGrapher.Save
import it.posteitaliane.omp.bl.MetricViewer.{ListOfRequestViews, ListOf}
import it.posteitaliane.omp.bl.FileReader.ProcessFile
import it.posteitaliane.omp.bl.FileReader.NewMetric
import it.posteitaliane.omp.UI.UIActor.UploadingFile


class MetricViewer extends Actor with Logging {
  this: EventSource =>
  var reader: ActorRef = context.system.deadLetters
  var grapher: ActorRef = context.system.deadLetters
  lazy val ui: ActorRef = Await.result((Metrics.director ? GiveMeUI).mapTo[ActorRef], 2.second)

  def receive = eventSourceReceiver orElse {
    case NewMetric(metric) => grapher ! Save(metric)
    case UploadingFile(filename) => reader ! ProcessFile(filename)
    case ListOf(WorkstationData) => (grapher ? LoadWorkstations).pipeTo(sender)
    case ListOf(ErrorData) => (grapher ? LoadErrors).pipeTo(sender)
    case ListOf(MethodData) => (grapher ? LoadMethods).pipeTo(sender)
    case ListOf(ServiceData) => (grapher ? LoadServices).pipeTo(sender)
    case ListOfRequestViews(es,met,ser,err) => (grapher ? LoadRequests(es,met,ser,err)).pipeTo(sender)

  }

  override def preStart() {
    reader = context.actorOf(FileReader.props, "fileReader")
    grapher = context.actorOf(MetricGrapher.props, "grapher")
    reader ! RegisterListener(self)
    grapher ! RegisterListener(self)
    ui ! RegisterListener(self)
  }
}

object MetricViewer {
  def props = Props(new MetricViewer with ProductionEventSource)

  case class ListOf(data: Data)

  case class ListOfRequestViews(ws: Iterable[Workstation] = Nil,
                                met: Iterable[Method] = Nil,
                                ser: Iterable[Service] = Nil,
                                err: Iterable[OmpError] = Nil)

}


