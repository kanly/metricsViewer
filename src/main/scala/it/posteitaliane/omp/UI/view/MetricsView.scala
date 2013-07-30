package it.posteitaliane.omp.UI.view

import com.vaadin.ui.{Notification, Upload, VerticalLayout}
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import java.io.{IOException, FileOutputStream, OutputStream}
import com.vaadin.server.Page
import akka.actor.{Props, Actor}
import it.posteitaliane.omp.UI.view.MetricsActor.FileUploaded
import it.posteitaliane.omp.UI.{SessionActor, Application}
import com.typesafe.scalalogging.slf4j.Logging

class MetricsView extends VerticalLayout with BaseView {
  setSizeFull()
  implicit val currentView = this
  val uploadReceiver = new MetricsHistoryUploader
  val upload = new Upload("Upload Metrics history file.", uploadReceiver)
  upload.addSucceededListener(uploadReceiver)

  addComponent(upload)

  def onEnter(event: ViewChangeEvent) {}
}

object MetricsView {
  def filenameToFullPath(filename: String) = s"${System.getProperty("java.io.tmpdir")}/$filename"
}

class MetricsHistoryUploader(implicit val currentView: BaseView) extends Upload.Receiver with Upload.SucceededListener {
  private var filename: String = null

  def receiveUpload(filename: String, mimeType: String): OutputStream = {
    try {
      this.filename = filename
      new FileOutputStream(MetricsView.filenameToFullPath(filename))
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
    currentView.actor ! FileUploaded(MetricsView.filenameToFullPath(filename))
  }
}


class MetricsActor(app:Application) extends Actor with Logging{
  def receive: Receive = {
    case FileUploaded(file) => {
      logger.debug(s"Uploaded file [$file]. SessionActor: [${app.sessionActor}")
      app.sessionActor ! SessionActor.FileReady(file)
    }
  }


}

object MetricsActor {
  def props(app:Application) = Props(new MetricsActor(app))

  case class FileUploaded(filename: String)

}
