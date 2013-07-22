package it.posteitaliane.omp.actor

import akka.actor.ActorSystem
import it.posteitaliane.omp.actor.MetricViewer.Uploaded
import com.typesafe.scalalogging.slf4j.Logging

object OMPSystem extends Logging{
  val sys = ActorSystem("omp")
  val viewer = sys.actorOf(MetricViewer.props,"viewer")

  def uploadedFile(filename: String) {
    viewer ! Uploaded(filename)
  }
}
