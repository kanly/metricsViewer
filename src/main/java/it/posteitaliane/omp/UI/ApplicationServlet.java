package it.posteitaliane.omp.UI;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(value = "/*", asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = it.posteitaliane.omp.scalaUI.Application.class, widgetset = "it.posteitaliane.omp.UI.AppWidgetSet")
public class ApplicationServlet extends VaadinServlet {
}
