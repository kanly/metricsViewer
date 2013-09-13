package it.posteitaliane.omp.UI

import scala.concurrent.duration._
import akka.util.Timeout

package object view {
  implicit val askTimeout = Timeout(5.seconds)
}
