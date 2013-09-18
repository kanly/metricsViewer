package it.posteitaliane.omp

import akka.actor.{Props, ActorSystem, Actor}
import it.posteitaliane.omp.UI.UIActor
import it.posteitaliane.omp.bl.MetricViewer
import it.posteitaliane.omp.Metrics.{GiveMeBE, GiveMeUI}

class Metrics extends Actor {
  var ui = context.system.deadLetters
  var be = context.system.deadLetters


  def receive: Receive = {
    case GiveMeUI => sender ! ui
    case GiveMeBE => sender ! be
  }

  override def preStart() {
    ui = context.actorOf(UIActor.props, "UI")
    be = context.actorOf(MetricViewer.props, "BE")
  }
}

object Metrics {
  val sys = ActorSystem("omp")
  val director = sys.actorOf(Props(new Metrics))

  case object GiveMeUI

  case object GiveMeBE

}
