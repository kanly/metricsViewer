package it.posteitaliane.omp.bl

import akka.actor.{Props, Actor}
import it.posteitaliane.omp.data._
import com.typesafe.scalalogging.slf4j.Logging
import it.posteitaliane.omp.bl.MetricGrapher._
import it.posteitaliane.omp.bl.MetricGrapher.Save
import it.posteitaliane.omp.data.Metric

class MetricGrapher extends Actor with Logging with MetricQueries {

  def receive = {
    case Save(metric) => save(metric)
    case LoadWorkstations => sender ! loadWorkstations
    case LoadMethods => sender ! loadMethods
    case LoadServices => sender ! loadServices
    case LoadErrors => sender ! loadErrors
  }
}

object MetricGrapher {
  def props = Props(new MetricGrapher)

  case class Save(metric: Metric)

  case object LoadWorkstations

  case object LoadMethods

  case object LoadServices

  case object LoadErrors

}
