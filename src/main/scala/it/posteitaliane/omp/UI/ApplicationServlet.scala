package it.posteitaliane.omp.UI

import javax.servlet.annotation.WebServlet
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinServlet

@WebServlet(value = Array("/*"), asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = classOf[Application], widgetset = "it.posteitaliane.omp.UI.AppWidgetSet")
class ApplicationServlet extends VaadinServlet