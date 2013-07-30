package it.posteitaliane.omp.UI.view

import com.vaadin.ui.{Label, Layout}
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.UI.{Application, CustomTheme, Views}
import akka.actor.ActorRef
import it.posteitaliane.omp.UI.SessionActor.GiveMeMyActor

import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration._


trait BaseView extends View with Logging {
  this: Layout =>

  val me = Views.fromType(getClass)
  if (me.isEmpty) throw UnregisteredView()


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
