package it.posteitaliane.omp.scalaUI.view

import com.vaadin.ui.{Notification, Upload, VerticalLayout}
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import java.io.{IOException, FileOutputStream, OutputStream}
import com.vaadin.server.Page
import it.posteitaliane.omp.actor.OMPSystem
import scala.Predef.String

class MetricsVIew extends VerticalLayout with BaseView {
  setSizeFull()
  val uploadReceiver = new MetricsHistoryUploader
  val upload = new Upload("Upload Metrics history file.", uploadReceiver)
  upload.addSucceededListener(uploadReceiver)


  addComponent(upload)

  def onEnter(event: ViewChangeEvent) {}


}

object MetricsView {
  def filenameToFullPath(filename: String) = s"${System.getProperty("java.io.tmpdir")}/$filename"
}

class MetricsHistoryUploader extends Upload.Receiver with Upload.SucceededListener {
  private var filename: String = null

  def receiveUpload(filename: String, mimeType: String): OutputStream = {
    try {
      this.filename = filename
      new FileOutputStream(MetricsView.filenameToFullPath(filename))
    }
    catch {
      case e: IOException => {
        new Notification("Could not open file <br/>", e.getMessage, Notification.Type.ERROR_MESSAGE).show(Page.getCurrent)
        return null
      }
    }
  }

  def uploadSucceeded(succeededEvent: Upload.SucceededEvent) {
    new Notification("Upload succeeded to " + System.getProperty("java.io.tmpdir"), Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    OMPSystem.uploadedFile(MetricsView.filenameToFullPath(filename))
  }


}
