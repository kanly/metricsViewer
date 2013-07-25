package it.posteitaliane.omp.bl

import akka.actor.{Props, ActorRef, Actor}
import it.posteitaliane.omp.bl.MetricViewer.Uploaded
import it.posteitaliane.omp.bl.FileReader.{NewMetric, ProcessFile}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.MetricGrapher.Save
import it.posteitaliane.omp.bl.ProductionEventSource.RegisterListener

class MetricViewer extends Actor with Logging {
  this: EventSource =>
  var reader: ActorRef = context.system.deadLetters
  var grapher: ActorRef = context.system.deadLetters

  def receive = eventSourceReceiver orElse {
    case Uploaded(filePath) => {
      reader ! ProcessFile(filePath)
    }
    case NewMetric(metric) => {
      grapher ! Save(metric)
    }

  }

  override def preStart() {
    reader = context.actorOf(FileReader.props, "fileReader")
    grapher = context.actorOf(MetricGrapher.props, "grapher")
    reader ! RegisterListener(self)
    grapher ! RegisterListener(self)
  }
}

object MetricViewer {

  case class Uploaded(filePath: String)


  def props = Props(new MetricViewer with ProductionEventSource)
}


