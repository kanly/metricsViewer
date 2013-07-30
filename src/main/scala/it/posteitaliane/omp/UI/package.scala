package it.posteitaliane.omp

import akka.util.Timeout
import scala.concurrent.duration._

package object UI {
  implicit val askTimeout = Timeout(5.seconds)
}
