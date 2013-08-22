package it.posteitaliane.omp.UI

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Status, Props, Actor, ActorRef}
import akka.pattern.ask
import it.posteitaliane.omp.UI.SessionActor._
import it.posteitaliane.omp.UI.view.BaseView
import it.posteitaliane.omp.data.{Data, WorkstationData, DTO, Workstation}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.UI.SessionActor.ViewChange
import it.posteitaliane.omp.UI.UIActor.Get
import it.posteitaliane.omp.UI.SessionActor.FileReady
import it.posteitaliane.omp.UI.SessionActor.GiveMeMyActor
import scala.util.{Failure, Success}
import it.posteitaliane.omp.bl.{ProductionEventSource, EventSource}

class SessionActor(ui: ActorRef, currApplication: Application) extends Actor with Logging {
  this: EventSource =>

  implicit val me = this
  implicit val application = currApplication
  val actors = Views.views.zip(Views.views.map(view => new LazyViewActor(view))).toMap

  def receive =  eventSourceReceiver orElse handleView(Views.HomeView)

  def handleView(currentView: View): Receive = {
    case Close => context.stop(self)
    case ViewChange(next) => {
      logger.debug("changing view")
      context.become(eventSourceReceiver orElse handleView(next))
    }
    case GiveMeMyActor(view) => sender ! actors(Views.fromView(view).get).actor
    case FileReady(file) => ui ! UIActor.FileReady(file)
    case LoadWorkstations => (ui ? Get(WorkstationData)).mapTo[List[Workstation]] onComplete {
      case Success(r) => {
        sendEvent(Updated[Workstation](WorkstationData, r))
        logger.debug("Workstations loaded")
      }
      case Failure(f) => {
        sender ! Status.Failure(f)
        logger.debug(s"Workstations loading failed $f")
      }
    }
  }
}

object SessionActor {
  def props(ui: ActorRef, currApplication: Application) = Props(new SessionActor(ui, currApplication) with ProductionEventSource)

  def sessionName(app: Application) = s"session_${app.getUIId}"

  case object Close

  case class ViewChange(nextView: View)

  case class GiveMeMyActor(view: BaseView)

  case class FileReady(filename: String)

  case object LoadWorkstations

  case class Updated[T <: DTO](dataType: Data, data: List[T])

}

class LazyViewActor(view: View)(implicit val father: Actor, implicit val application: Application) {
  lazy val actor = view.actorFactory(father, application)
}
