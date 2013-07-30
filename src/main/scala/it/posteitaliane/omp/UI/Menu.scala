package it.posteitaliane.omp.UI

import com.vaadin.ui.MenuBar
import com.vaadin.navigator.Navigator
import com.vaadin.ui.MenuBar.Command
import it.posteitaliane.omp.UI.view.{MetricsActor, MetricsView, HomeView, BaseView}
import com.vaadin.navigator
import akka.actor.{Actor, ActorRef}
import it.posteitaliane.omp.UI.SessionActor.ViewChange
import com.typesafe.scalalogging.slf4j.Logging

class Menu(navigator: Navigator) extends MenuBar {
  Views.views.foreach(v => addItem(v.name, NavigateTo(v, navigator)))
}

case class NavigateTo(targetView: View, navigator: Navigator) extends Command with Logging {
  def menuSelected(p1: MenuBar#MenuItem) {
    logger.debug(s"switching to view ${targetView.toString}")
    navigator.navigateTo(targetView.urlString)
    Application.getCurrent.sessionActor ! ViewChange(targetView)
  }
}

case class View(clazz: Class[_ <: BaseView], name: String, urlString: String, actorFactory: (Actor, Application) => ActorRef)

object Views {
  val HomeView = View(classOf[HomeView], "home", "index", {
    (a: Actor, application:Application) => a.context.system.deadLetters
  })
  val views: List[View] = List(
    HomeView,
    View(classOf[MetricsView], "metrics", "Metrics", {
      (a: Actor, application:Application) => a.context.actorOf(MetricsActor.props(application))
    })
  )

  def fromType(clazz: Class[_ <: BaseView]) = {
    views.find(_.clazz == clazz)
  }

  def fromView(view: BaseView) = {
    fromType(view.getClass)
  }

  def fromPlainView(view: navigator.View) = {
    views.find(_.clazz == view.getClass)
  }
}