package it.posteitaliane.omp.scalaUI.view

import com.vaadin.ui.VerticalLayout
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent

class HomeView extends VerticalLayout with BaseView {
  setSizeFull()

  def onEnter(event: ViewChangeEvent) {}
}
