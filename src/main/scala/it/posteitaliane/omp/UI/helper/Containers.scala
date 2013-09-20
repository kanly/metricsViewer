package it.posteitaliane.omp.UI.helper

import com.vaadin.data.Property


object Containers {
  implicit def convertProperty[T](prop: Property[_]): Property[T] = prop.asInstanceOf[Property[T]]

  def setPropValue[T](prop:Property[T],value:T) {
    prop.setValue(value)
  }
}
