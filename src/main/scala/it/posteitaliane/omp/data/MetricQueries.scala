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

  def loadElements[T](query: String, mapper: MapFunc[T]): List[T] = executeQuery(query).map(mapper)

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
    "request" -> metric.request,
    "startTime" -> Long.box(metric.startTime),
    "endTime" -> Long.box(metric.endTime),
    "success" -> (if (metric.success) "OK" else "KO"),
    "layer" -> metric.layer
  ))

}

object Mapper {
  type MapFunc[T] = Map[String, AnyRef] => T

  def workstationMapper(key: String): MapFunc[Workstation] =
    Mapper.genericMapper((node: Node) => Workstation(getStringProperty(node, Keys.workstationFrazionario), getStringProperty(node, Keys.workstationPdl)))(key)

  def methodMapper(key: String): MapFunc[Method] =
    Mapper.genericMapper(node => Method(getStringProperty(node, Keys.methodName)))(key)

  def serviceMapper(key: String): MapFunc[Service] =
    Mapper.genericMapper(node => Service(getStringProperty(node, Keys.serviceName)))(key)

  def errorMapper(key: String): MapFunc[OmpError] =
    Mapper.genericMapper(node => OmpError(getStringProperty(node, Keys.errorCode)))(key)

  def genericMapper[A](mapper: Node => A)(key: String)(record: Map[String, AnyRef]): A = {
    record match {
      case (record: Map[String, AnyRef]) => record(key) match {
        case workstationNode: Node => mapper(workstationNode)
        case _ => throw new UnsupportedOperationException("Unexpected type in query result")
      }
    }
  }

  def getStringProperty(workstationNode: Node, frazionario: String): String = {
    workstationNode.getProperty(frazionario).asInstanceOf[String]
  }

}
