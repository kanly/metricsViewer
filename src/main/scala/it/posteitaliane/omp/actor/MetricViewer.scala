package it.posteitaliane.omp.actor

import akka.actor.{Props, ActorRef, Actor}
import it.posteitaliane.omp.actor.MetricViewer.Uploaded
import it.posteitaliane.omp.actor.FileReader.ProcessFile
import com.typesafe.scalalogging.slf4j.Logging

class MetricViewer extends Actor with Logging {
  var reader: ActorRef = context.system.deadLetters

  def receive = {
    case Uploaded(filePath) => {
      reader ! ProcessFile(filePath)
    }

  }

  override def preStart() {
    reader = context.actorOf(Props(new FileReader), "fileReader")
  }
}

object MetricViewer {

  case class Uploaded(filePath: String)

  def props = Props(new MetricViewer)
}


