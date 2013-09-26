package it.posteitaliane.omp.UI.view

import it.posteitaliane.omp.UI.helper.Improvements._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import com.vaadin.ui._
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.{Sizeable, Page}
import akka.actor.Props
import it.posteitaliane.omp.UI.{CustomTheme, Application}
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data._
import it.posteitaliane.omp.UI.view.MetricsActor.{LoadMetrics, NeedData}
import com.vaadin.data.Property.ValueChangeEvent
import it.posteitaliane.omp.UI.SessionActor.{LoadRequestViews, Load, Updated}
import it.posteitaliane.omp.data

import it.posteitaliane.omp.UI.helper.Listeners._
import it.posteitaliane.omp.UI.helper.Containers._
import com.vaadin.data.util.{HierarchicalContainer, IndexedContainer}
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.{Failure, Success}
import com.vaadin.event.ItemClickEvent
import com.vaadin.data.Property

class MetricsView extends VerticalLayout with BaseView {
  logger.debug("Instantiating MetricsView")

  setSizeFull()
  val left=new VerticalLayout()
  val right=new HorizontalLayout()
  private val splitPanel: HorizontalSplitPanel = new HorizontalSplitPanel(left, right)
  splitPanel.setSplitPosition(300,Sizeable.Unit.PIXELS)
  splitPanel.setLocked(true)
  splitPanel.addStyleName(CustomTheme.SplitpanelSmall)
  addComponent(splitPanel)


  val loadMetricsTable = (event: ValueChangeEvent) => {
    logger.debug(s"Value changed to ${event.getProperty.getValue}")
    new Notification(s"Value changed to ${event.getProperty.getValue}", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent)
    loadMetrics()
  }
  var metricTable: Table = null

  val wsTree = new TreeTable()
  wsTree.setHeight(200, Sizeable.Unit.PIXELS)
  wsTree.setWidth(250,Sizeable.Unit.PIXELS)
  wsTree.setMultiSelect(true)
  wsTree.setSelectable(true)
  wsTree.setImmediate(true)
  wsTree.addValueChangeListener(loadMetricsTable)

  val methodTree = new TreeTable()
  methodTree.setHeight(200, Sizeable.Unit.PIXELS)
  methodTree.setWidth(250,Sizeable.Unit.PIXELS)
  methodTree.setMultiSelect(true)
  methodTree.setSelectable(true)
  methodTree.setImmediate(true)
  methodTree.addValueChangeListener(loadMetricsTable)

  val errorTable = new Table()
  errorTable.setHeight(200, Sizeable.Unit.PIXELS)
  errorTable.setWidth(250,Sizeable.Unit.PIXELS)
  errorTable.setMultiSelect(true)
  errorTable.setSelectable(true)
  errorTable.setImmediate(true)
  errorTable.addValueChangeListener(loadMetricsTable)

  left.addComponents(wsTree,methodTree,errorTable)

  def onEnter(event: ViewChangeEvent) {
    actor ! NeedData
  }

  def loadMetrics() {
    val workstations: Iterable[WorkstationView] = wsTree.getScalaValue

    val wssView: Iterable[WorkstationView] = if (workstations.nonEmpty)
      workstations.groupBy {
        case WorkstationView(fraz, pdls) => fraz
      }.map {
        case (fraz, wsList) => {
          val pdls = wsList.map(_.pdls).foldLeft(List[String]())((a, b) => a ++ b).filter(_.toInt >= 0)
          WorkstationView(fraz, pdls)
        }
      }
    else
      Nil

    val methods: Iterable[MethodView] = methodTree.getScalaValue

    val metView: Iterable[MethodView] = if (methods.nonEmpty)
      methods.groupBy {
        case MethodView(service, method) => service
      }.map {
        case (serv, metViewList) =>
          val methods = metViewList.map(_.methods).foldLeft(List[String]())((a, b) => a ++ b)
          MethodView(serv, methods)
      }
    else
      Nil

    actor ! LoadMetrics(ws = wssView,
      met = metView,
      err = errorTable.getScalaValue
    )
  }

  def updateWorkstations(app: Application, workstations: List[WorkstationView]) {
    app.access(new Runnable {
      def run() {
        logger.debug("Updating workstations")
        val container = new HierarchicalContainer()
        container.addContainerProperty("Workstation", classOf[String], "-")
        container.addContainerProperty("ws", classOf[WorkstationView], null)
        workstations.foreach(ws => {
          val frazWs: WorkstationView = WorkstationView(ws.frazionario, Nil)
          val frazItem = container.addItem(frazWs)
          setPropValue(frazItem.getItemProperty("Workstation"), frazWs.frazionario)

          ws.pdls.foreach(pdl => {
            val pdlWs = WorkstationView(ws.frazionario, List(pdl))
            val frazItem = container.addItem(pdlWs)
            setPropValue(frazItem.getItemProperty("Workstation"), pdl)
            container.setParent(pdlWs, frazWs)
            container.setChildrenAllowed(pdlWs, false)
          })

          wsTree.setContainerDataSource(container)
          wsTree.setVisibleColumns("Workstation")
        })
      }
    })
  }

  def updateErrors(app: Application, errors: List[data.OmpError]) {
    app.access(new Runnable {
      def run() {
        logger.debug("Updating errors")
        val container = new IndexedContainer()
        container.addContainerProperty("Error", classOf[String], "-")

        errors.foreach(err => {
          val errorItem = container.addItem(err)
          setPropValue(errorItem.getItemProperty("Error"), err.code)
        })
        errorTable.setContainerDataSource(container)
        errorTable.setVisibleColumns("Error")
      }
    })
  }

  def updateMethods(app: Application, methods: List[MethodView]) {
    app.access(new Runnable {
      def run() {
        logger.debug(s"Updating methods")
        val container = new HierarchicalContainer()
        container.addContainerProperty("Method", classOf[String], "-")

        methods.foreach(met => {
          val serviceView = MethodView(met.service, Nil)
          val serviceItem = container.addItem(serviceView)
          setPropValue(serviceItem.getItemProperty("Method"), serviceView.service)

          met.methods.foreach(method => {
            val methodView = MethodView(met.service, List(method))
            val methodItem = container.addItem(methodView)
            setPropValue(methodItem.getItemProperty("Method"), method)
            container.setParent(methodView, serviceView)
            container.setChildrenAllowed(methodView, false)
          })

          methodTree.setContainerDataSource(container)
          methodTree.setVisibleColumns("Method")
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
        newTable.setEnabled(true)
        newTable.addItemClickListener((event: ItemClickEvent) => {
          val property: Property[RequestView] = event.getItem.getItemProperty(MetricsContainer.fullBeanKey)
          val window = new DetailWindow(property.getValue)
          UI.getCurrent.addWindow(window)
        })
        newTable.setVisibleColumns(wsKey, methodKey, serviceKey, errorKey, stKey, etKey, layerKey, successKey)
        right.replaceComponent(metricTable, newTable)
        metricTable = newTable
        logger.debug("Table created")
      }
    })
  }

}

class MetricsActor(app: Application) extends ViewActor[MetricsView](app) with Logging {
  def receiveForView(view: MetricsView): Receive = {
    case NeedData => {
      app.sessionActor ! Load(WorkstationData)
      app.sessionActor ! Load(ErrorData)
      app.sessionActor ! Load(MethodData)
    }
    case Updated(WorkstationData, wStations: List[WorkstationView@unchecked]) => view.updateWorkstations(app, wStations)
    case Updated(MethodData, methods: List[MethodView@unchecked]) => view.updateMethods(app, methods)
    case Updated(ErrorData, errors: List[OmpError@unchecked]) => view.updateErrors(app, errors)
    case Updated(dataType, data) => logger.debug(s"Unmanaged updated data type: $dataType")
    case LoadMetrics(ws, met, err) => (app.sessionActor ? LoadRequestViews(ws, met, err)).mapTo[List[RequestView]] onComplete {
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
}

object MetricsActor {
  def props(app: Application) = Props(new MetricsActor(app))

  case class LoadMetrics(ws: Iterable[WorkstationView] = Nil,
                         met: Iterable[MethodView] = Nil,
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

class DetailWindow(item: RequestView) extends Window {
  val layout = new FormLayout()
  val dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS dd-MM-yyyy")
  setContent(layout)
  setWidth(500, Sizeable.Unit.PIXELS)
  setHeight(500, Sizeable.Unit.PIXELS)

  addTextField("Frazionario", item.ws.frazionario)
  addTextField("PDL", item.ws.pdl)
  addTextField("Service", item.service.serviceName)
  addTextField("Method", item.method.methodName)
  addTextField("Layer", item.request.layer)
  addTextField("Start", dateFormatter.format(new Date(item.request.startTime)))
  addTextField("End", dateFormatter.format(new Date(item.request.endTime)))
  addTextField("Success", item.request.success)
  if (item.error != null)
    addTextField("Error", item.error.code)

  private val reqField = new TextArea("Request")
  reqField.setValue(item.request.request)
  reqField.setSizeFull()
  reqField.setRows(10)
  layout.addComponent(reqField)


  def addTextField(caption: String, value: String) {
    val field = new TextField(caption)
    field.setValue(value)
    layout.addComponent(field)
  }


}
