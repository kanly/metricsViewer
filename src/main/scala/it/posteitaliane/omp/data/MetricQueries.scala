package it.posteitaliane.omp.data

import com.typesafe.scalalogging.slf4j.Logging
import org.neo4j.graphdb.Node
import Mapper.MapFunc

trait MetricQueries extends GraphDB with Logging {

  def loadWorkstations: List[WorkstationView] =
    loadElements("START poff=node:postalOffice('*:*') MATCH poff-[has:Own]->wor RETURN wor", Mapper.workstationMapper("wor"))
      .groupBy {
      case Workstation(fraz, pdl) => fraz
    }.map {
      case (fraz, wss) => WorkstationView(fraz, wss.map(ws => ws.pdl).sorted)
    }.toList.sortBy(wsv => wsv.frazionario)


  def loadMethods: List[MethodView] =
    loadElements("START ser=node:service('*:*') MATCH ser-[own:Own]->met RETURN ser,met", Mapper.serviceMethodMapper("ser", "met"))
      .map {
      case (service, method) => (service.serviceName, method.methodName)
    }.groupBy {
      case (service, _) => service
    }.map {
      case (service, methods) => MethodView(service, methods.map {
        case (_, method) => method
      }.sorted)
    }.toList

  def loadErrors: List[OmpError] =
    loadElements("START err=node:error('*:*') RETURN err", Mapper.errorMapper("err"))

  def loadRequests(workstations: Iterable[WorkstationView] = Nil,
                   methods: Iterable[MethodView] = Nil,
                   errors: Iterable[OmpError] = Nil
                    ): List[RequestView] = {

    def buildPdl(pdls: List[String]): String = {
      if (pdls.nonEmpty) {
        pdls.map(pdl => s"wor.${Keys.workstationPdl}=${"\"" + pdl + "\""}").mkString("AND (", " OR ", ")")
      } else ""
    }

    def buildMethods(methods: List[String]): String = {
      if (methods.nonEmpty)
        methods.map(met => s"met.${Keys.methodName}=${"\"" + met + "\""}").mkString("AND (", " OR ", ")")
      else ""
    }

    logger.debug(s"workstations: ${workstations.mkString(";")} \nmethods: ${methods.mkString(";")}\nerrors: ${errors.mkString(";")}")

    val clause: StringBuilder = new StringBuilder()

    if (workstations.nonEmpty) {
      clause.append(
        workstations.map(ws => {
          s"(poff.${Keys.workstationFrazionario}=${"\"" + ws.frazionario + "\""} ${buildPdl(ws.pdls)})"

        }).mkString("(", " OR ", ")")
      )
    }

    if (methods.nonEmpty) {
      if (clause.nonEmpty) clause.append(" AND ")
      clause.append(
        methods.map(meth => s"(ser.${Keys.serviceName}=${"\"" + meth.service + "\""} ${buildMethods(meth.methods)})").mkString("(", " OR ", ")")
      )
    }

    if (errors.nonEmpty) {
      if (clause.nonEmpty) clause.append(" AND ")
      clause.append(
        errors.map(err => s"err.${Keys.errorCode}=${"\"" + err.code + "\""}").mkString("(", " OR ", ")")
      )
    }

    val query = s"START poff=node:postalOffice('*:*') MATCH poff-[has:Own]->wor-[exe:Execute]->req<-[execBy:ExecutedBy]-met<-[own:Own]-ser, poff-[has:Own]->wor-[execute:Execute]->req<-[thr?:ThrownBy]-err ${if (clause.nonEmpty) s"WHERE $clause" else ""} RETURN wor,req,met,ser,err"


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

      val postalOfficeNode = createOrLoadNode(PostalOfficeIndex(metric.frazionario), Map(Keys.workstationFrazionario -> metric.frazionario))

      val workstationNode = createOrLoadNode(WorkStationIndex((metric.frazionario, metric.pdl)), Map(Keys.workstationFrazionario -> metric.frazionario, Keys.workstationPdl -> metric.pdl),
        onCreate = newWsNode => addRelationship(postalOfficeNode, newWsNode, Own)
      )

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

  def serviceMethodMapper(serviceKey: String, methodKey: String): MapFunc[(Service, Method)] =
    (record: Map[String, AnyRef]) => Some(
      serviceMapper(serviceKey)(record).orNull,
      methodMapper(methodKey)(record).orNull
    )


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
