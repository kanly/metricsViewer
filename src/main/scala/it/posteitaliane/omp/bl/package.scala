package it.posteitaliane.omp

import scala.concurrent.duration._
import akka.util.Timeout

package object bl {
  implicit val askTimeout = Timeout(2.seconds)
}
