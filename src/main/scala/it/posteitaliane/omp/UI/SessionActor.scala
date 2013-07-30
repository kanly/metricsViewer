package it.posteitaliane.omp.UI

import akka.actor.{Props, Actor, ActorRef}
import it.posteitaliane.omp.UI.SessionActor.{FileReady, GiveMeMyActor, ViewChange, Close}
import it.posteitaliane.omp.UI.view.BaseView
import it.posteitaliane.omp.UI.UIActor.Get
import it.posteitaliane.omp.data.Workstation
import com.typesafe.scalalogging.slf4j.Logging

class SessionActor(ui: ActorRef, currApplication: Application) extends Actor with Logging {
  implicit val me = this
  implicit val application = currApplication
  val actors = Views.views.zip(Views.views.map(view => new LazyViewActor(view))).toMap

  def receive = handleView(Views.HomeView)

  def handleView(currentView: View): Receive = {
    case Close => context.stop(self)
    case ViewChange(next) => {
      logger.debug("changing view")
      context.become(handleView(next))
      ui ! Get(Workstation)
      logger.debug("changing view")
    }
    case GiveMeMyActor(view) => sender ! actors(Views.fromView(view).get).actor
    case FileReady(file) => ui ! UIActor.FileReady(file)
  }
}

object SessionActor {
  def props(ui: ActorRef, currApplication: Application) = Props(new SessionActor(ui, currApplication))

  def sessionName(app: Application) = s"session_${app.getUIId}"

  case object Close

  case class ViewChange(nextView: View)

  case class GiveMeMyActor(view: BaseView)

  case class FileReady(filename: String)

}

class LazyViewActor(view: View)(implicit val father: Actor, implicit val application: Application) {
  lazy val actor = view.actorFactory(father, application)
}
