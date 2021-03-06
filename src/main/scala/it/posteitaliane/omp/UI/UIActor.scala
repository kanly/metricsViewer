package it.posteitaliane.omp.UI

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{ActorRef, Props, Actor}
import it.posteitaliane.omp.UI.UIActor.{ClearAllData, Get, UploadingFile, NewSession}
import it.posteitaliane.omp.bl.{ProductionEventSource, EventSource}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data.{DTO, Data}
import it.posteitaliane.omp.bl.MetricViewer.{DropData, ListOfRequestViews, ListOf}
import it.posteitaliane.omp.Metrics
import it.posteitaliane.omp.Metrics.GiveMeBE

import akka.pattern.{ask, pipe}
import scala.concurrent.Await
import scala.concurrent.duration._
import it.posteitaliane.omp.bl.ProductionEventSource.RegisterListener
import it.posteitaliane.omp.bl.MetricGrapher.DataUpdated
import it.posteitaliane.omp.UI.SessionActor.LoadRequestViews

class UIActor extends Actor with Logging {
  this: EventSource =>

  val exCont = global // Using this to make the import useful for intellij so it will not remove import when optimizing

  lazy val be = Await.result((Metrics.director ? GiveMeBE).mapTo[ActorRef], 2.second)

  def receive = eventSourceReceiver orElse {
    case NewSession(app) => sender ! context.actorOf(SessionActor.props(self, app))
    case UploadingFile(filename) => sendEvent(UploadingFile(filename))
    case Get(data) =>
      (be ? ListOf(data)).pipeTo(sender)
    case du: DataUpdated[DTO@unchecked] => sendEvent(du)
    case LoadRequestViews(ws, met, err) =>
      (be ? ListOfRequestViews(ws, met, err)).pipeTo(sender)
    case ClearAllData => be ! DropData
  }

  override def preStart() {
    Application.uiActor = self
    logger.debug(s"self: ${self.toString()}")
    logger.debug(s"uiActor: ${Application.uiActor.toString()}")
    logger.debug("PreStart UIActor completo")
    be ! RegisterListener(self)
  }
}

object UIActor {
  def props = Props(new UIActor with ProductionEventSource)

  case class NewSession(application: Application)

  case class UploadingFile(filename: String)

  case class Get(data: Data)

  case object ClearAllData

}
