package it.posteitaliane.omp.UI;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

@Theme(CustomTheme.THEME_NAME)
@SuppressWarnings("serial")
public class Application extends UI {

    private final VerticalSplitPanel mainLayout = new VerticalSplitPanel();
    private final VerticalLayout headerLayout = new VerticalLayout();
    private final VerticalLayout contentLayout = new VerticalLayout();
    private final Navigator navigator = new Navigator(this, contentLayout);

    @Override
    protected void init(VaadinRequest request) {

        configureMainLayout();
        buildNavigator();
        addHeader();
        mainLayout.setSecondComponent(contentLayout);
        navigator.navigateTo(Views.HOME.urlStr);
    }

    private void configureMainLayout() {
        mainLayout.setSizeFull();
        mainLayout.setLocked(true);
        mainLayout.addStyleName("main");
        mainLayout.addStyleName(CustomTheme.SPLITPANEL_SMALL);
        mainLayout.setSplitPosition(23, Unit.PIXELS);
        setContent(mainLayout);
    }

    private void buildNavigator() {
        for (Views view : Views.validViews()) {
            navigator.addView(view.urlStr, view.viewType);
        }
    }

    private void addHeader() {
        headerLayout.addComponent(new Menu(navigator));
        mainLayout.setFirstComponent(headerLayout);
    }

}
