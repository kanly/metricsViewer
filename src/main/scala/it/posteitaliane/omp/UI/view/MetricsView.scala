package it.posteitaliane.omp.UI.view

import it.posteitaliane.omp.UI.helper.Improvements._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import com.vaadin.ui._
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import java.io.{IOException, FileOutputStream, OutputStream}
import com.vaadin.server.Page
import akka.actor.{Props, Actor}
import it.posteitaliane.omp.UI.{SessionActor, Application}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data._
import it.posteitaliane.omp.UI.view.MetricsActor.{LoadMetrics, NeedData, FileUploaded, RegisterView}
import com.vaadin.data.Property.ValueChangeEvent
import it.posteitaliane.omp.UI.SessionActor.{LoadRequestViews, ViewChanged, Load, Updated}
import it.posteitaliane.omp.data.Workstation
import it.posteitaliane.omp.bl.ProductionEventSource.{UnregisterListener, RegisterListener}
import it.posteitaliane.omp.data

import it.posteitaliane.omp.UI.helper.Listeners._
import it.posteitaliane.omp.UI.helper.Containers._
import com.vaadin.data.util.IndexedContainer
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.{Failure, Success}

class MetricsView extends VerticalLayout with BaseView {
  logger.debug("Instantiating MetricsView")
  actor ! MetricsActor.RegisterView(this)
  setSizeFull()
  implicit val currentView = this
  val uploadReceiver = new MetricsHistoryUploader
  val upload = new Upload("Upload Metrics history file.", uploadReceiver)
  upload.addSucceededListener(uploadReceiver)
  var metricTable: Table = null

  addComponent(upload)

  val workstationsSelect = new ListSelect("workstations")
  workstationsSelect.setRows(10)
  workstationsSelect.setNullSelectionAllowed(true)
  workstationsSelect.setImmediate(true)
  workstationsSelect.setMultiSelect(true)
  workstationsSelect.addValueChangeListener((event: ValueChangeEvent) => {
    logger.debug(s"Value changed to ${event.getProperty.getValue}")
    new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    actor ! LoadMetrics(ws = workstationsSelect.getScalaValue,
      met = methodsSelect.getScalaValue,
      ser = servicesSelect.getScalaValue,
      err = errorsSelect.getScalaValue
    )
  })

  val methodsSelect = new ListSelect("methods")
  methodsSelect.setRows(10)
  methodsSelect.setNullSelectionAllowed(true)
  methodsSelect.setImmediate(true)
  methodsSelect.setMultiSelect(true)
  methodsSelect.addValueChangeListener((event: ValueChangeEvent) => {
    logger.debug(s"Value changed to ${event.getProperty.getValue}")
    new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    actor ! LoadMetrics(ws = workstationsSelect.getScalaValue,
      met = methodsSelect.getScalaValue,
      ser = servicesSelect.getScalaValue,
      err = errorsSelect.getScalaValue
    )
  })

  val servicesSelect = new ListSelect("services")
  servicesSelect.setRows(10)
  servicesSelect.setNullSelectionAllowed(true)
  servicesSelect.setImmediate(true)
  servicesSelect.setMultiSelect(true)
  servicesSelect.addValueChangeListener((event: ValueChangeEvent) => {
    logger.debug(s"Value changed to ${event.getProperty.getValue}")
    new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    actor ! LoadMetrics(ws = workstationsSelect.getScalaValue,
      met = methodsSelect.getScalaValue,
      ser = servicesSelect.getScalaValue,
      err = errorsSelect.getScalaValue
    )
  })

  val errorsSelect = new ListSelect("errors")
  errorsSelect.setRows(10)
  errorsSelect.setNullSelectionAllowed(true)
  errorsSelect.setImmediate(true)
  errorsSelect.setMultiSelect(true)
  errorsSelect.addValueChangeListener((event: ValueChangeEvent) => {
    logger.debug(s"Value changed to ${event.getProperty.getValue}")
    new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    actor ! LoadMetrics(ws = workstationsSelect.getScalaValue,
      met = methodsSelect.getScalaValue,
      ser = servicesSelect.getScalaValue,
      err = errorsSelect.getScalaValue
    )
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
        logger.debug("Updating services")
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
        logger.debug("Updating methods")
        methodsSelect.removeAllItems()
        methods.foreach(me => {
          methodsSelect.addItem(me)
          methodsSelect.setItemCaption(me, s"name: ${me.methodName}")
        })
      }
    })
  }

  def addMetricsTable(app: Application, requests: List[RequestView]) {
    app.access(new Runnable {

      import MetricsContainer._

      def run() {
        logger.debug("Creating metricsTable")
        val newTable = new Table("Requests", MetricsContainer(requests))
        newTable.setVisibleColumns(wsKey, methodKey, serviceKey, errorKey, stKey, etKey, layerKey, successKey)
        replaceComponent(metricTable, newTable)
        metricTable = newTable
        logger.debug("Table created")
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
    case LoadMetrics(ws, met, ser, err) => (app.sessionActor ? LoadRequestViews(ws, met, ser, err)).mapTo[List[RequestView]] onComplete {
      case Success(result) => {
        logger.debug(s"Received ${result.size} metrics")
        view.addMetricsTable(app, result)
        logger.debug("Metrics Updated")
      }
      case Failure(f) => {
        app.notify(s"Something wrong while loading metrics: ${f.getMessage}")
        logger.warn("Something wrong while loading metrics", f.getCause)
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

  case class LoadMetrics(ws: Iterable[Workstation] = Nil,
                         met: Iterable[Method] = Nil,
                         ser: Iterable[Service] = Nil,
                         err: Iterable[OmpError] = Nil)

  case object NeedData

}


class MetricsContainer(items: List[RequestView]) extends IndexedContainer with Logging {

  val dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS dd-MM-yyyy")

  addContainerProperty(MetricsContainer.wsKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.methodKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.serviceKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.errorKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.stKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.etKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.layerKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.successKey, classOf[String], "---")
  addContainerProperty(MetricsContainer.fullBeanKey, classOf[RequestView], null)

  items.foreach(requestView => {
    val item = getItem(addItem())

    setPropValue[String](item.getItemProperty(MetricsContainer.wsKey), s"${requestView.ws.frazionario}-${requestView.ws.pdl}")
    setPropValue[String](item.getItemProperty(MetricsContainer.methodKey), requestView.method.methodName)
    setPropValue[String](item.getItemProperty(MetricsContainer.serviceKey), requestView.service.serviceName)

    val error: OmpError = requestView.error
    if (error != null)
      setPropValue[String](item.getItemProperty(MetricsContainer.errorKey), error.code)

    val request: Request = requestView.request
    setPropValue[String](item.getItemProperty(MetricsContainer.stKey), dateFormatter.format(new Date(request.startTime)))
    setPropValue[String](item.getItemProperty(MetricsContainer.etKey), dateFormatter.format(new Date(request.endTime)))
    setPropValue[String](item.getItemProperty(MetricsContainer.layerKey), request.layer)
    setPropValue[String](item.getItemProperty(MetricsContainer.successKey), request.success)

    setPropValue[RequestView](item.getItemProperty(MetricsContainer.fullBeanKey), requestView)

  })

}

object MetricsContainer {
  val fullBeanKey = "requestView"
  val wsKey = "Workstation"
  val methodKey = "Method"
  val serviceKey = "Service"
  val errorKey = "Error"
  val stKey = "startTime"
  val etKey = "endTime"
  val layerKey = "layer"
  val successKey = "success"

  val keys = List(wsKey, methodKey, serviceKey, errorKey, stKey, etKey, layerKey, successKey)

  def apply(items: List[RequestView]) = new MetricsContainer(items)
}
