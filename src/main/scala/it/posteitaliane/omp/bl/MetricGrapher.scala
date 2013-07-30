package it.posteitaliane.omp.bl

import akka.actor.{Props, Actor}
import it.posteitaliane.omp.data._
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.MetricGrapher._
import it.posteitaliane.omp.bl.MetricGrapher.Save
import it.posteitaliane.omp.data.MethodIndex
import it.posteitaliane.omp.data.WorkStationIndex
import it.posteitaliane.omp.data.ErrorIndex
import it.posteitaliane.omp.data.Metric
import it.posteitaliane.omp.data.ServiceIndex

class MetricGrapher extends Actor with Logging with GraphDB {


  def receive = {
    case Save(metric) => save(metric)
    case LoadWorkstations => executeQuery( """START wor=node:workstation('*:*') RETURN wor""").foreach(record => {
      logger.debug(record.toString())
    })
    case LoadMethods => executeQuery( """START met=node:method('*:*') RETURN met""")
    case LoadServices => executeQuery( """START ser=node:service('*:*') RETURN ser""")
    case LoadErrors => executeQuery( """START err=node:error('*:*') RETURN err""")
  }

  def save(metric: Metric) {
    beginTx()
    try {
      val requestNode = createRequestNode(metric)

      val serviceNode = createOrLoadNode(ServiceIndex(metric.serviceName), Map("name" -> metric.serviceName))

      val methodNode = createOrLoadNode(MethodIndex(metric.methodName), Map("name" -> metric.methodName),
        onCreate = newMethodNode => addRelationship(serviceNode, newMethodNode, Own)
      )

      val workstationNode = createOrLoadNode(WorkStationIndex((metric.frazionario, metric.pdl)), Map("frazionario" -> metric.frazionario, "pdl" -> metric.pdl))

      addRelationship(methodNode, requestNode, ExecutedBy)
      addRelationship(workstationNode, requestNode, Execute)

      if (!metric.success) {
        val errorNode = createOrLoadNode(ErrorIndex(metric.errorCode), Map("code" -> metric.errorCode))
        addRelationship(errorNode, requestNode, ThrownBy, Map("message" -> metric.errorMessage))
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

object MetricGrapher {
  def props = Props(new MetricGrapher)

  case class Save(metric: Metric)

  case object LoadWorkstations

  case object LoadMethods

  case object LoadServices

  case object LoadErrors


}
