package it.posteitaliane.omp.UI

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Status, Props, Actor, ActorRef}
import akka.pattern.{ask, pipe}
import it.posteitaliane.omp.UI.SessionActor._
import it.posteitaliane.omp.UI.view.BaseView
import it.posteitaliane.omp.data._
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.{ProductionEventSource, EventSource}
import it.posteitaliane.omp.UI.UIActor.UploadingFile
import it.posteitaliane.omp.UI.SessionActor.ViewChanged
import it.posteitaliane.omp.UI.SessionActor.Load
import scala.util.Failure
import it.posteitaliane.omp.UI.UIActor.Get
import it.posteitaliane.omp.data.Workstation
import it.posteitaliane.omp.UI.SessionActor.GiveMeMyActor
import it.posteitaliane.omp.bl.ProductionEventSource.RegisterListener
import it.posteitaliane.omp.bl.MetricGrapher.DataUpdated
import scala.util.Success
import it.posteitaliane.omp.data.Method
import it.posteitaliane.omp.UI.SessionActor.ViewChange
import it.posteitaliane.omp.UI.SessionActor.Updated
import it.posteitaliane.omp.data.Service
import it.posteitaliane.omp.UI.SessionActor.FileReady

class SessionActor(ui: ActorRef, currApplication: Application) extends Actor with Logging {
  this: EventSource =>

  implicit val me = this
  implicit val application = currApplication
  val actors = Views.views.zip(Views.views.map(view => new LazyViewActor(view))).toMap

  def receive = eventSourceReceiver orElse handleView(Views.HomeView)

  def handleView(currentView: View): Receive = {
    case Close => context.stop(self)
    case ViewChange(next) => {
      logger.debug("changing view")
      context.become(eventSourceReceiver orElse handleView(next))
      sendEvent(ViewChanged(currentView, next))
    }
    case GiveMeMyActor(view) => sender ! actors(Views.fromView(view).get).actor
    case FileReady(file) => ui ! UIActor.UploadingFile(file)
    case Load(data) => (ui ? Get(data)).mapTo[List[DTO]] onComplete {
      case Success(r) => {
        sendEvent(Updated[DTO](data, r))
        logger.debug("Workstations loaded")
      }
      case Failure(f) => {
        sender ! Status.Failure(f)
        logger.debug(s"Workstations loading failed $f")
      }
    }
    case UploadingFile(_) => currApplication.notify("A new history file is being uploaded and processed")
    case DataUpdated(dataType, data) => sendEvent(Updated(dataType, data))
    case LoadRequestViews(ws, met, ser, err) => (ui ? LoadRequestViews(ws, met, ser, err)).pipeTo(sender)
  }

  override def preStart() {
    ui ! RegisterListener(self)
  }
}

object SessionActor {
  def props(ui: ActorRef, currApplication: Application) = Props(new SessionActor(ui, currApplication) with ProductionEventSource)

  case object Close

  case class ViewChange(nextView: View)

  case class GiveMeMyActor(view: BaseView)

  case class FileReady(filename: String)

  case class Load(dataType: Data)

  case class Updated[T <: DTO](dataType: Data, data: List[T])

  case class ViewChanged(oldView: View, nextView: View)

  case class LoadRequestViews(ws: Iterable[Workstation] = Nil,
                              met: Iterable[Method] = Nil,
                              ser: Iterable[Service] = Nil,
                              err: Iterable[OmpError] = Nil)

}

class LazyViewActor(view: View)(implicit val father: Actor, implicit val application: Application) {
  lazy val actor = view.actorFactory(father, application)
}
