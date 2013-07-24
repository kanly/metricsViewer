package it.posteitaliane.omp.UI

import com.vaadin.annotations.{VaadinServletConfiguration, Theme}
import com.vaadin.ui.{UI, VerticalLayout, VerticalSplitPanel}
import com.vaadin.navigator.Navigator
import com.vaadin.server.Sizeable.Unit
import com.vaadin.server.{VaadinServlet, VaadinRequest}
import com.typesafe.scalalogging.slf4j.Logging
import javax.servlet.annotation.WebServlet

@Theme(CustomTheme.ThemeName)
class Application extends UI with Logging {
  val mainLayout = new VerticalSplitPanel()
  val headerLayout = new VerticalLayout
  val contentLayout = new VerticalLayout
  val navigator = new Navigator(this, contentLayout)

  logger.info("Application created and... I am scala")

  override def init(request: VaadinRequest) {
    configureMainLayout()
    buildNavigator()
    addHeader()
    mainLayout.setSecondComponent(contentLayout)
    navigator.navigateTo(Views.HomeView.urlString)
  }

  private def configureMainLayout() {
    mainLayout.setSizeFull()
    mainLayout.setLocked(true)
    mainLayout.addStyleName("main")
    mainLayout.addStyleName(CustomTheme.SplitpanelSmall)
    mainLayout.setSplitPosition(23, Unit.PIXELS)
    setContent(mainLayout)
  }

  private def buildNavigator() {
    Views.views.foreach {
      case View(viewType, _, urlStr) => navigator.addView(urlStr, viewType)
    }
  }

  private def addHeader() {
    headerLayout.addComponent(new Menu(navigator))
    mainLayout.setFirstComponent(headerLayout)
  }
}

@WebServlet(value = Array("/*"), asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = classOf[Application], widgetset = "it.posteitaliane.omp.UI.AppWidgetSet")
class ApplicationServlet extends VaadinServlet

object CustomTheme {
  final val ViewTitle = "viewTitle"

  /* FROM RUNO THEME */
  final val ThemeName = "runo"
  final val ButtonSmall = "small"
  val ButtonBig = "big"
  val ButtonDefault = "default"
  val PanelLight = "light"
  val TabsheetSmall = "light"
  val SplitpanelReduced = "rounded"
  val SplitpanelSmall = "small"
  val LabelH1 = "h1"
  val LabelH2 = "h2"
  val LabelSmall = "small"
  val LayoutDarker = "darker"
  val CsslayoutShadow = "box-shadow"
  val CssLayoutSelectable = "selectable"
  val CssLayoutSelectableSelected = "selectable-selected"
  val TextfieldSmall = "small"
  val TableSmall = "small"
  val TableBorderless = "borderless"
  val AccordionLight = "light"
  val WindowDialog = "dialog"
  /* FROM BASE THEME */
  val ButtonLink = "link"
  val TreeConnectors = "connectors"
  val Clip = "v-clip"
}
