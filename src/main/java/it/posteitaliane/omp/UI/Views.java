package it.posteitaliane.omp.UI;

import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.vaadin.navigator.View;
import it.posteitaliane.omp.UI.view.BaseView;
import it.posteitaliane.omp.UI.view.HomeView;
import it.posteitaliane.omp.UI.view.MetricsView;

import java.util.Collection;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Sets.newHashSet;

public enum Views {

    NULL(null, null, null),
    HOME(HomeView.class, "home", "index"),
    METRICS(MetricsView.class, "metrics", "Metrics");

    private static final Collection<Views> notNullViews = Collections2.filter(newHashSet(values()), not(equalTo(NULL)));
    public final Class<? extends BaseView> viewType;
    public final String viewName;
    public final String urlStr;

    private Views(Class<? extends BaseView> viewType, String viewName, String urlStr) {

        this.urlStr = urlStr;
        this.viewName = viewName;
        this.viewType = viewType;
    }

    public static Views from(@SuppressWarnings("rawtypes") Class<? extends BaseView> viewType) {
        for (Views view : values()) {
            if (view.viewType == viewType)
                return view;
        }
        return NULL;
    }

    public static Views from(View view) {
        if ((view == null) || !(view instanceof BaseView<?>))
            return NULL;
        return from(((BaseView<?>) view).getClass());
    }

    public static FluentIterable<Views> validViews() {
        return FluentIterable.from(notNullViews);
    }
}
