package it.posteitaliane.omp.UI.helper

import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import com.vaadin.event.ItemClickEvent
import com.vaadin.event.ItemClickEvent.ItemClickListener
import com.vaadin.ui.Button.{ClickEvent, ClickListener}


object Listeners {

  implicit def toValueChangeListener(func: ValueChangeEvent => Unit): ValueChangeListener = new ValueChangeListener {
    def valueChange(event: ValueChangeEvent) {
      func(event)
    }
  }

  implicit def toItemClickListener(func: ItemClickEvent => Unit): ItemClickListener = new ItemClickListener {
    def itemClick(event: ItemClickEvent) {
      func(event)
    }
  }

  implicit def toButtonClickListener(func: ClickEvent => Unit) = new ClickListener {
    def buttonClick(event: ClickEvent) {
      func(event)
    }
  }
}
