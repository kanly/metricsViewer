package it.posteitaliane.omp.actor

import akka.actor.{Props, ActorRef, Actor}
import it.posteitaliane.omp.actor.MetricViewer.{NewMetric, Uploaded}
import it.posteitaliane.omp.actor.FileReader.ProcessFile
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data.Metric
import it.posteitaliane.omp.actor.MetricGrapher.Save

class MetricViewer extends Actor with Logging {
  var reader: ActorRef = context.system.deadLetters
  var grapher:ActorRef = context.system.deadLetters

  def receive = {
    case Uploaded(filePath) => {
      reader ! ProcessFile(filePath)
    }
    case NewMetric(metric) => {
      grapher ! Save(metric)
    }

  }

  override def preStart() {
    reader = context.actorOf(Props(new FileReader), "fileReader")
    grapher = context.actorOf(MetricGrapher.props,"grapher")
  }
}

object MetricViewer {

  case class Uploaded(filePath: String)
  case class NewMetric(metric:Metric)

  def props = Props(new MetricViewer)
}


