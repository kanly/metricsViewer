package it.posteitaliane.omp.UI.view;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.VerticalLayout;

public class HomeView extends BaseView<VerticalLayout> {
    public HomeView() {
        super(new VerticalLayout());
        setSizeFull();
    }

    @Override
    protected void onEnter(ViewChangeListener.ViewChangeEvent event) {
    }


}
