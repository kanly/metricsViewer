package it.posteitaliane.omp.scalaUI

import com.vaadin.ui.MenuBar
import com.vaadin.navigator.Navigator
import com.vaadin.ui.MenuBar.Command
import it.posteitaliane.omp.scalaUI.view.{MetricsVIew, HomeView, BaseView}
import com.vaadin.navigator

class Menu(navigator: Navigator) extends MenuBar {
  Views.views.foreach(v => addItem(v.name, NavigateTo(v, navigator)))
}

case class NavigateTo(targetView: View, navigator: Navigator) extends Command {
  def menuSelected(p1: MenuBar#MenuItem) {
    navigator.navigateTo(targetView.urlString)
  }
}

case class View(clazz: Class[_ <: BaseView], name: String, urlString: String)

object Views {
  val HomeView = View(classOf[HomeView], "home", "index")
  val views: List[View] = List(
    HomeView,
    View(classOf[MetricsVIew], "metrics", "Metrics")
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