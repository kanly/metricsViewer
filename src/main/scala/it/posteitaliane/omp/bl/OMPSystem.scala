package it.posteitaliane.omp.bl

import akka.actor.ActorSystem
import it.posteitaliane.omp.bl.MetricViewer.Uploaded
import com.typesafe.scalalogging.slf4j.Logging

object OMPSystem extends Logging{
  val sys = ActorSystem("omp")
  val viewer = sys.actorOf(MetricViewer.props,"viewer")

  def uploadedFile(filename: String) {
    viewer ! Uploaded(filename)
  }
}
