package it.posteitaliane.omp.UI;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.MenuBar;

public class Menu extends MenuBar {
    private final Navigator navigator;

    public Menu(Navigator navigator) {
        this.navigator = navigator;
        for (Views view : Views.validViews()) {
            addItem(view.viewName, new NavigateTo(view, navigator));
        }
    }

    private static class NavigateTo implements Command {
        private final String urlStr;
        private final Navigator navigator;

        public NavigateTo(Views targetView, Navigator navigator) {
            urlStr = targetView.urlStr;
            this.navigator = navigator;
        }

        @Override
        public void menuSelected(MenuItem menuItem) {
            navigator.navigateTo(urlStr);
        }
    }

}
