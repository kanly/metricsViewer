package it.posteitaliane.omp.UI.view

import com.vaadin.ui._
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import java.io.{IOException, FileOutputStream, OutputStream}
import com.vaadin.server.Page
import akka.actor.{Props, Actor}
import it.posteitaliane.omp.UI.{SessionActor, Application}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data.{WorkstationData, Workstation}
import it.posteitaliane.omp.UI.view.MetricsActor.FileUploaded
import it.posteitaliane.omp.UI.view.MetricsActor.RegisterView
import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import it.posteitaliane.omp.bl.ProductionEventSource.RegisterListener
import it.posteitaliane.omp.UI.SessionActor.Updated

class MetricsView extends VerticalLayout with BaseView {
  actor ! MetricsActor.RegisterView(this)
  setSizeFull()
  implicit val currentView = this
  val uploadReceiver = new MetricsHistoryUploader
  val upload = new Upload("Upload Metrics history file.", uploadReceiver)
  upload.addSucceededListener(uploadReceiver)

  addComponent(upload)

  val workstationsSelect = new ListSelect("workstations")
  workstationsSelect.setRows(10)
  workstationsSelect.setNullSelectionAllowed(true)
  workstationsSelect.setImmediate(true)
  workstationsSelect.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      logger.debug(s"Value changed to ${event.getProperty.getValue}")
      new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    }
  })
  addComponent(workstationsSelect)

  def onEnter(event: ViewChangeEvent) {}

  def updateWorkstations(app: Application, workstations: List[Workstation]) {
    app.access(new Runnable {
      def run() {
        logger.debug("Updating workstations")
        workstationsSelect.removeAllItems()
        workstations.foreach(ws => {
          workstationsSelect.addItem(ws)
          workstationsSelect.setItemCaption(ws, s"F:${ws.frazionario};P:${ws.pdl}")
        })
      }
    })
  }
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


class MetricsActor(app: Application) extends Actor with Logging {
  def receive: Receive = {
    case RegisterView(view) => context.become(receiveForView(view))
  }

  def receiveForView(view: MetricsView): Receive = {
    case FileUploaded(file) => {
      logger.debug(s"Uploaded file [$file]. SessionActor: [${app.sessionActor}")
      app.sessionActor ! SessionActor.FileReady(file)
    }
    case Updated(WorkstationData,wStations:List[Workstation]) => view.updateWorkstations(app, wStations)
    case Updated(dataType,data) => logger.debug(s"Unmanaged updated data type: $dataType")
  }

  override def preStart() {
    app.sessionActor ! RegisterListener(self)
  }

}

object MetricsActor {
  def props(app: Application) = Props(new MetricsActor(app))

  case class FileUploaded(filename: String)

  case class RegisterView(view: MetricsView)

}
