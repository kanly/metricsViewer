package it.posteitaliane.omp.UI.helper

import com.vaadin.ui.ListSelect
import java.util
import scala.collection.JavaConverters._


object Improvements {
  implicit def toBetterListSelect(select:ListSelect):BetterListSelect = new BetterListSelect(select)
  implicit def fromBetterListSelect(better:BetterListSelect):ListSelect = better.select

}

class BetterListSelect(val select:ListSelect) {
  def getScalaValue[T]:Iterable[T]={
    if(select.isMultiSelect)
      select.getValue.asInstanceOf[util.Collection[T]].asScala
    else
      Set(select.getValue.asInstanceOf[T])
  }
}
