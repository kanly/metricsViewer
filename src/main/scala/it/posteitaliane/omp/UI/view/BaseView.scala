package it.posteitaliane.omp.UI.view

import com.vaadin.ui.{Label, Layout}
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.UI.{Application, CustomTheme, Views}
import akka.actor.{Actor, ActorRef}
import it.posteitaliane.omp.UI.SessionActor.{ViewChanged, GiveMeMyActor}

import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._
import it.posteitaliane.omp.bl.ProductionEventSource.{UnregisterListener, RegisterListener}
import it.posteitaliane.omp.UI.view.ViewActor.RegisterView


trait BaseView extends View with Logging {
  this: Layout =>

  val me = Views.fromType(getClass)
  if (me.isEmpty) throw UnregisteredView()
  actor ! RegisterView(this)
  implicit val currentView = this

  final def enter(event: ViewChangeEvent) {
    logger.trace(s"Entering in view ${me.get.name} from view ${Views.fromPlainView(event.getOldView)} with parameters [${event.getParameters}")
    onEnter(event)
  }

  protected def addViewTitle() {
    val title = new Label(me.get.name)
    title.addStyleName(CustomTheme.ViewTitle)
    addComponent(title)
  }

  def onEnter(event: ViewChangeEvent)

  def actor: ActorRef = Await.result((Application.getCurrent.sessionActor ? GiveMeMyActor(this)).mapTo[ActorRef], 1.second)
}

case class UnregisteredView extends java.lang.Error

abstract class ViewActor[V <: BaseView](app: Application) extends Actor with Logging{
  def receive: Receive = waitingForView

  def waitingForView: Receive = {
    case RegisterView(view: V@unchecked) => {
      context.become(viewChange(view) orElse receiveForView(view))
      app.sessionActor ! RegisterListener(self)
    }
  }

  def viewChange(view:V):Receive = {
    case ViewChanged(oldView, newView) => {
      if (view.me.get != newView) {
        logger.debug("changing view....")
        context.become(waitingForView)
        app.sessionActor ! UnregisterListener(self)
      }
    }
  }

  def receiveForView(view: V): Receive
}

object ViewActor {

  case class RegisterView[V <: BaseView](view: V)

}
