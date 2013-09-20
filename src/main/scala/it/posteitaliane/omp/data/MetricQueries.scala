package it.posteitaliane.omp.data

import com.typesafe.scalalogging.slf4j.Logging
import org.neo4j.graphdb.Node
import Mapper.MapFunc

trait MetricQueries extends GraphDB with Logging {

  def loadWorkstations: List[Workstation] =
    loadElements("START wor=node:workstation('*:*') RETURN wor", Mapper.workstationMapper("wor"))

  def loadMethods: List[Method] =
    loadElements("START met=node:method('*:*') RETURN met", Mapper.methodMapper("met"))

  def loadServices: List[Service] =
    loadElements("START ser=node:service('*:*') RETURN ser", Mapper.serviceMapper("ser"))

  def loadErrors: List[OmpError] =
    loadElements("START err=node:error('*:*') RETURN err", Mapper.errorMapper("err"))

  def loadRequests(workstations: Iterable[Workstation] = Nil,
                   methods: Iterable[Method] = Nil,
                   services: Iterable[Service] = Nil,
                   errors: Iterable[OmpError] = Nil
                    ): List[RequestView] = {

    logger.debug(s"workstations: ${workstations.mkString(";")} \nmethods: ${methods.mkString(";")}\nservices: ${services.mkString(";")}\nerrors: ${errors.mkString(";")}")

    val clause: StringBuilder = new StringBuilder()

    clause.append(if (workstations.nonEmpty) {
      workstations.map(ws => s"(wor.${Keys.workstationFrazionario}=${"\"" + ws.frazionario + "\""} AND wor.${Keys.workstationPdl}=${"\"" + ws.pdl + "\""})").mkString("(", " OR ", ")")
    })

    if (methods.nonEmpty) {
      if (clause.nonEmpty) clause.append(" AND ")
      clause.append(
        methods.map(met => s"met.${Keys.methodName}=${"\"" + met.methodName + "\""}").mkString("(", " OR ", ")")
      )
    }

    if (services.nonEmpty) {
      if (clause.nonEmpty) clause.append(" AND ")
      clause.append(
        services.map(ser => s"ser.${Keys.serviceName}=${"\"" + ser.serviceName + "\""}").mkString("(", " OR ", ")")
      )
    }

    if (errors.nonEmpty) {
      if (clause.nonEmpty) clause.append(" AND ")
      clause.append(
        errors.map(err => s"err.${Keys.errorCode}=${"\"" + err.code + "\""}").mkString("(", " OR ", ")")
      )
    }

    val query = s"START wor=node:workstation('*:*') MATCH wor-[exe:Execute]->req<-[execBy:ExecutedBy]-met<-[own:Own]-ser, wor-[execute:Execute]->req<-[thr?:ThrownBy]-err ${if (clause.nonEmpty) s"WHERE $clause" else ""} RETURN wor,req,met,ser,err"


    logger.debug(s"requests query: $query")

    val elements: List[RequestView] = loadElements(query, Mapper.requestViewMapper("wor", "req", "met", "ser", "err"))
    logger.debug(s"found ${elements.size} requests")
    logger.trace(elements.mkString("; "))
    elements
  }

  def loadElements[T](query: String, mapper: MapFunc[T]): List[T] = executeQuery(query).map(mapper).filter(_.isDefined).map(_.get)

  def save(metric: Metric) {
    beginTx()
    try {
      val requestNode = createRequestNode(metric)

      val serviceNode = createOrLoadNode(ServiceIndex(metric.serviceName), Map(Keys.serviceName -> metric.serviceName))

      val methodNode = createOrLoadNode(MethodIndex(metric.methodName), Map(Keys.methodName -> metric.methodName),
        onCreate = newMethodNode => addRelationship(serviceNode, newMethodNode, Own)
      )

      val workstationNode = createOrLoadNode(WorkStationIndex((metric.frazionario, metric.pdl)), Map(Keys.workstationFrazionario -> metric.frazionario, Keys.workstationPdl -> metric.pdl))

      addRelationship(methodNode, requestNode, ExecutedBy)
      addRelationship(workstationNode, requestNode, Execute)

      if (!metric.success) {
        val errorNode = createOrLoadNode(ErrorIndex(metric.errorCode), Map(Keys.errorCode -> metric.errorCode))
        addRelationship(errorNode, requestNode, ThrownBy, Map(Keys.thrownByMessage -> metric.errorMessage))
      }

      logger.debug(s"Successfully saved metric: ${metric.toString}")
      successTx()
    } catch {
      case e: Exception => logger.warn(s"Cannot save metric: ${metric.toString}.", e)
    } finally {
      finishTx()
    }
  }

  def createRequestNode(metric: Metric) = createNode(Map(
    Keys.request -> metric.request,
    Keys.requestStart -> Long.box(metric.startTime),
    Keys.requestEnd -> Long.box(metric.endTime),
    Keys.requestSuccess -> (if (metric.success) "OK" else "KO"),
    Keys.requestLayer -> metric.layer
  ))

}

object Mapper {

  type MapFunc[T] = Map[String, AnyRef] => Option[T]

  def workstationMapper(key: String): MapFunc[Workstation] =
    Mapper.genericMapper((node: Node) => Workstation(getStringProperty(node, Keys.workstationFrazionario), getStringProperty(node, Keys.workstationPdl)))(key)

  def methodMapper(key: String): MapFunc[Method] =
    Mapper.genericMapper(node => Method(getStringProperty(node, Keys.methodName)))(key)

  def serviceMapper(key: String): MapFunc[Service] =
    Mapper.genericMapper(node => Service(getStringProperty(node, Keys.serviceName)))(key)

  def errorMapper(key: String): MapFunc[OmpError] =
    Mapper.genericMapper(node => OmpError(getStringProperty(node, Keys.errorCode)))(key)

  def requestMapper(key: String): MapFunc[Request] =
    genericMapper(node => Request(
      getStringProperty(node, Keys.request),
      getLongProperty(node, Keys.requestStart),
      getLongProperty(node, Keys.requestEnd),
      getStringProperty(node, Keys.requestLayer),
      getStringProperty(node, Keys.requestSuccess))
    )(key)

  def requestViewMapper(workstationKey: String, requestKey: String, methodKey: String, serviceKey: String, errorKey: String): MapFunc[RequestView] =
    (record: Map[String, AnyRef]) => Some(RequestView(
      workstationMapper(workstationKey)(record).orNull,
      methodMapper(methodKey)(record).orNull,
      serviceMapper(serviceKey)(record).orNull,
      errorMapper(errorKey)(record).orNull,
      requestMapper(requestKey)(record).orNull
    ))

  def genericMapper[A](mapper: Node => A)(key: String)(record: Map[String, AnyRef]): Option[A] = {
    record match {
      case (record: Map[String, AnyRef]) => record.get(key) match {
        case Some(node: Node) => Some(mapper(node))
        case Some(null) => None
        case Some(a) => throw new UnsupportedOperationException(s"Unexpected type in query result $a")
        case None => None
      }
    }
  }

  def getStringProperty(workstationNode: Node, prop: String): String = {
    workstationNode.getProperty(prop).asInstanceOf[String]
  }

  def getLongProperty(workstationNode: Node, prop: String): Long =
    workstationNode.getProperty(prop).asInstanceOf[java.lang.Long]

}
