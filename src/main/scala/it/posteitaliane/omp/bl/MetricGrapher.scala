package it.posteitaliane.omp.bl

import akka.actor.{Props, Actor}
import it.posteitaliane.omp.data._
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.data.Metric
import it.posteitaliane.omp.bl.MetricGrapher.Save
import it.posteitaliane.omp.data.Method

class MetricGrapher extends Actor with Logging with GraphDB {


  def receive = {
    case Save(metric) => {
      beginTx
      try {
        val requestNode = createRequestNode(metric)
        val methodNode = createOrLoadNode(Method(metric.methodName), Map("name" -> metric.methodName))
        val serviceNode = createOrLoadNode(Service(metric.serviceName), Map("name" -> metric.serviceName))
        val frazionarioNode = createOrLoadNode(Frazionario(metric.frazionario), Map("code" -> metric.frazionario))
        val pdlNode = createOrLoadNode(Pdl(metric.pdl), Map("code" -> metric.pdl))
        addRelationship(methodNode, serviceNode, Own)
        addRelationship(serviceNode, methodNode, Belong)
        addRelationship(frazionarioNode, pdlNode, Own)
        addRelationship(pdlNode, frazionarioNode, Belong)
        addRelationship(pdlNode, requestNode, Execute)
        addRelationship(requestNode, pdlNode, RequestedBy)
        addRelationship(methodNode, requestNode, ExecutedBy)
        addRelationship(requestNode, methodNode, Execute)
        if (!metric.success) {
          val errorNode = createOrLoadNode(Error(metric.errorCode), Map("code" -> metric.errorCode))
          addRelationship(errorNode, requestNode, ThrownBy, Map("message" -> metric.errorMessage))
          addRelationship(requestNode, errorNode, Throw, Map("message" -> metric.errorMessage))
        }
        logger.debug(s"Successfuly saved metric: ${metric.toString}")
        successTx
      } catch {
        case e: Exception => logger.warn(s"Cannot save metric: ${metric.toString}.", e)
      } finally {
        finishTx
      }
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

}
