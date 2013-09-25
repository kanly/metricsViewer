package it.posteitaliane.omp.UI.view

import com.vaadin.ui.{Button, Notification, Upload, VerticalLayout}
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import it.posteitaliane.omp.UI.{SessionActor, Application}
import java.io.{IOException, FileOutputStream, OutputStream}
import com.vaadin.server.Page
import it.posteitaliane.omp.UI.view.HomeActor.{PurgeDB, FileUploaded}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.UI.helper.Listeners._
import com.vaadin.ui.Button.ClickEvent
import it.posteitaliane.omp.UI.UIActor.ClearAllData
import akka.actor.Props

class HomeView extends VerticalLayout with BaseView {
  setSizeFull()

  val uploadReceiver = new MetricsHistoryUploader
  val upload = new Upload("Upload Metrics history file.", uploadReceiver)
  upload.addSucceededListener(uploadReceiver)

  addComponent(upload)

  val purgeDb = new Button("Purge Data")
  purgeDb.addClickListener((event: ClickEvent) => {
    actor ! PurgeDB
  })

  addComponent(purgeDb)

  def onEnter(event: ViewChangeEvent) {}
}


class HomeActor(app: Application) extends ViewActor[HomeView](app) with Logging {

  def receiveForView(view: HomeView): Receive = {
    case FileUploaded(file) => {
      logger.debug(s"Uploaded file [$file]. SessionActor: [${app.sessionActor}")
      app.sessionActor ! SessionActor.FileReady(file)
    }
    case PurgeDB => {
      logger.info("Purging DB")
      app.sessionActor ! ClearAllData
    }
  }
}

object HomeActor {
  def props(app: Application) = Props(new HomeActor(app))

  case class FileUploaded(filename: String)

  case object PurgeDB

}

class MetricsHistoryUploader(implicit val currentView: BaseView) extends Upload.Receiver with Upload.SucceededListener {
  private var filename: String = null

  def receiveUpload(filename: String, mimeType: String): OutputStream = {
    try {
      this.filename = filename
      new FileOutputStream(filenameToFullPath(filename))
    }
    catch {
      case e: IOException => {
        new Notification("Could not open file <br/>", e.getMessage, Notification.Type.ERROR_MESSAGE).show(Page.getCurrent)
        null
      }
    }
  }

  def uploadSucceeded(succeededEvent: Upload.SucceededEvent) {
    new Notification("Upload succeeded to " + System.getProperty("java.io.tmpdir"), Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    currentView.actor ! FileUploaded(filenameToFullPath(filename))
  }

  def filenameToFullPath(filename: String) = s"${System.getProperty("java.io.tmpdir")}/$filename"
}



