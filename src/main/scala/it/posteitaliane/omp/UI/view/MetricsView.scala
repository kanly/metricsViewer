package it.posteitaliane.omp.UI.view

import com.vaadin.ui._
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import java.io.{IOException, FileOutputStream, OutputStream}
import com.vaadin.server.Page
import akka.actor.{Props, Actor}
import it.posteitaliane.omp.UI.{SessionActor, Application}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data._
import it.posteitaliane.omp.UI.view.MetricsActor.{NeedData, FileUploaded, RegisterView}
import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import it.posteitaliane.omp.UI.SessionActor.{ViewChanged, Load, Updated}
import it.posteitaliane.omp.data.Workstation
import it.posteitaliane.omp.bl.ProductionEventSource.{UnregisterListener, RegisterListener}
import it.posteitaliane.omp.data

class MetricsView extends VerticalLayout with BaseView {
  logger.debug("Instantiating MetricsView")
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

  val methodsSelect = new ListSelect("methods")
  methodsSelect.setRows(10)
  methodsSelect.setNullSelectionAllowed(true)
  methodsSelect.setImmediate(true)
  methodsSelect.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      logger.debug(s"Value changed to ${event.getProperty.getValue}")
      new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    }
  })

  val servicesSelect = new ListSelect("services")
  servicesSelect.setRows(10)
  servicesSelect.setNullSelectionAllowed(true)
  servicesSelect.setImmediate(true)
  servicesSelect.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      logger.debug(s"Value changed to ${event.getProperty.getValue}")
      new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    }
  })

  val errorsSelect = new ListSelect("errors")
  errorsSelect.setRows(10)
  errorsSelect.setNullSelectionAllowed(true)
  errorsSelect.setImmediate(true)
  errorsSelect.addValueChangeListener(new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      logger.debug(s"Value changed to ${event.getProperty.getValue}")
      new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    }
  })
  addComponent(new HorizontalLayout(workstationsSelect, methodsSelect, servicesSelect, errorsSelect))

  def onEnter(event: ViewChangeEvent) {
    actor ! NeedData
  }

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

  def updateErrors(app: Application, errors: List[data.OmpError]) {
    app.access(new Runnable {
      def run() {
        logger.debug("Updating errors")
        errorsSelect.removeAllItems()
        errors.foreach(er => {
          errorsSelect.addItem(er)
          errorsSelect.setItemCaption(er, s"C: ${er.code}")
        })
      }
    })
  }

  def updateServices(app: Application, services: List[Service]) {
    app.access(new Runnable {
      def run() {
        logger.debug("Updating errors")
        servicesSelect.removeAllItems()
        services.foreach(ser => {
          servicesSelect.addItem(ser)
          servicesSelect.setItemCaption(ser, s"name: ${ser.serviceName}")
        })
      }
    })
  }

  def updateMethods(app: Application, methods: List[Method]) {
    app.access(new Runnable {
      def run() {
        logger.debug("Updating errors")
        methodsSelect.removeAllItems()
        methods.foreach(me => {
          methodsSelect.addItem(me)
          methodsSelect.setItemCaption(me, s"name: ${me.methodName}")
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
  def receive: Receive = waitingForView

  def waitingForView: Receive = {
    case RegisterView(view) => {
      context.become(receiveForView(view))
      app.sessionActor ! RegisterListener(self)
    }
  }

  def receiveForView(view: MetricsView): Receive = {
    case FileUploaded(file) => {
      logger.debug(s"Uploaded file [$file]. SessionActor: [${app.sessionActor}")
      app.sessionActor ! SessionActor.FileReady(file)
    }
    case NeedData => {
      app.sessionActor ! Load(WorkstationData)
      app.sessionActor ! Load(ErrorData)
      app.sessionActor ! Load(MethodData)
      app.sessionActor ! Load(ServiceData)
    }
    case Updated(WorkstationData, wStations: List[Workstation@unchecked]) => view.updateWorkstations(app, wStations)
    case Updated(ServiceData, services: List[Service@unchecked]) => view.updateServices(app, services)
    case Updated(MethodData, methods: List[Method@unchecked]) => view.updateMethods(app, methods)
    case Updated(ErrorData, errors: List[OmpError@unchecked]) => view.updateErrors(app, errors)
    case Updated(dataType, data) => logger.debug(s"Unmanaged updated data type: $dataType")
    case ViewChanged(oldView, newView) => {
      if (view.me.get != newView) {
        logger.debug("changing view....")
        context.become(waitingForView)
        app.sessionActor ! UnregisterListener(self)
      }
    }
  }

  override def preStart() {
    app.sessionActor ! RegisterListener(self)
  }

}

object MetricsActor {
  def props(app: Application) = Props(new MetricsActor(app))

  case class FileUploaded(filename: String)

  case class RegisterView(view: MetricsView)

  case object NeedData

}
