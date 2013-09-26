package it.posteitaliane.omp.UI.helper

import com.vaadin.ui.AbstractSelect
import java.util
import scala.collection.JavaConverters._


object Improvements {
  implicit def toBetterListSelect[T <: AbstractSelect](select: T): BetterSelect[T] = new BetterSelect(select)

  implicit def fromBetterListSelect[T <: AbstractSelect](better: BetterSelect[T]): T = better.select

}

class BetterSelect[S <: AbstractSelect](val select: S) {
  def getScalaValue[T]: Iterable[T] = {
    if (select.isMultiSelect)
      select.getValue.asInstanceOf[util.Collection[T]].asScala
    else
      Set(select.getValue.asInstanceOf[T])
  }
}
