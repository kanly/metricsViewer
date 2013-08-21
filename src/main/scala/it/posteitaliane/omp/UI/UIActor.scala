package it.posteitaliane.omp.UI

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{ActorRef, Props, Actor}
import it.posteitaliane.omp.UI.UIActor.{Get, FileReady, NewSession}
import it.posteitaliane.omp.bl.{ProductionEventSource, EventSource}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data.{DTO, Data}
import it.posteitaliane.omp.bl.MetricViewer.ListOf
import it.posteitaliane.omp.Metrics
import it.posteitaliane.omp.Metrics.GiveMeBE

import akka.pattern.{ask, pipe}
import scala.concurrent.Await
import scala.concurrent.duration._

class UIActor extends Actor with Logging {
  this: EventSource =>

  lazy val be = Await.result((Metrics.director ? GiveMeBE).mapTo[ActorRef], 2.second)

  def receive = eventSourceReceiver orElse {
    case NewSession(app) => sender ! context.actorOf(SessionActor.props(self, app), SessionActor.sessionName(app))
    case FileReady(filename) => sendEvent(FileReady(filename))
    case Get(data) => (be ? ListOf(data)).pipeTo(sender)
  }

  override def preStart() {
    Application.uiActor = self
    logger.debug(s"self: ${self.toString()}")
    logger.debug(s"uiActor: ${Application.uiActor.toString()}")
    logger.debug("PreStart UIActor completo")
  }
}

object UIActor {
  def props = Props(new UIActor with ProductionEventSource)

  case class NewSession(application: Application)

  case class FileReady(filename: String)

  case class Get(data: Data)

}
