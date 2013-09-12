package it.posteitaliane.omp.UI.helper

import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}


object Listeners {

  implicit def toValueChangeListener(func: ValueChangeEvent => Unit): ValueChangeListener = new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      func(event)
    }
  }
}
