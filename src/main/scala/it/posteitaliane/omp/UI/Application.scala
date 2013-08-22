package it.posteitaliane.omp.UI

import com.vaadin.annotations.{Push, Theme}
import com.vaadin.ui.{Notification, UI, VerticalLayout, VerticalSplitPanel}
import com.vaadin.navigator.Navigator
import com.vaadin.server.Sizeable.Unit
import com.vaadin.server.{Page, VaadinRequest}
import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.ActorRef
import akka.pattern.ask
import it.posteitaliane.omp.UI.UIActor.NewSession
import it.posteitaliane.omp.Metrics
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import akka.util.Timeout

@Theme(CustomTheme.ThemeName)
@Push
class Application extends UI with Logging {
  implicit val askTimeout = Timeout(5.seconds)
  val mainLayout = new VerticalSplitPanel()
  val headerLayout = new VerticalLayout
  val contentLayout = new VerticalLayout
  val navigator = new Navigator(this, contentLayout)
  logger.debug(s"Application.uiActor=${Application.uiActor}")

  lazy val sessionActor = {
    logger.debug(s"Application.uiActor=${Application.uiActor}")
    val future: Future[ActorRef] = (Application.uiActor ? NewSession(this)).mapTo[ActorRef]
    Await.result(future, 5.second)
  }

  override def init(request: VaadinRequest) {
    logger.debug("init")
    configureMainLayout()
    buildNavigator()
    addHeader()
    mainLayout.setSecondComponent(contentLayout)
    navigator.navigateTo(Views.HomeView.urlString)
  }

  override def close() {
    logger.debug("closing")
    super.close()
    logger.debug("closed")
  }

  private def configureMainLayout() {
    mainLayout.setSizeFull()
    mainLayout.setLocked(true)
    mainLayout.addStyleName("main")
    mainLayout.addStyleName(CustomTheme.SplitpanelSmall)
    mainLayout.setSplitPosition(23, Unit.PIXELS)
    setContent(mainLayout)
  }

  private def buildNavigator() {
    Views.views.foreach {
      case View(viewType, _, urlStr, _) => navigator.addView(urlStr, viewType)
    }
  }

  private def addHeader() {
    headerLayout.addComponent(new Menu(navigator))
    mainLayout.setFirstComponent(headerLayout)
  }

  import Notification.Type.TRAY_NOTIFICATION

  def notify(message: String, notificationType: Notification.Type = TRAY_NOTIFICATION) {
    access(new Runnable {
      def run() {
        new Notification(message, notificationType).show(Page.getCurrent)
      }
    })
  }
}

object Application {
  var uiActor: ActorRef = Metrics.sys.deadLetters

  def getCurrent = UI.getCurrent.asInstanceOf[Application]

}





