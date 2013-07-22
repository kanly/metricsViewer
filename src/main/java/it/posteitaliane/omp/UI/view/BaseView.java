package it.posteitaliane.omp.UI.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.vaadin.ui.*;
import it.posteitaliane.omp.UI.CustomTheme;
import it.posteitaliane.omp.UI.Views;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ClientMethodInvocation;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.server.ServerRpcManager;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.shared.communication.SharedState;

public abstract class BaseView<T extends Layout> implements View, Layout {
    private static final long serialVersionUID = 1L;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final T backingLayout;
    protected final Views me;

    public BaseView(T implementedLayout) {
        this.backingLayout = implementedLayout;
        this.me = Views.from(getClass());
        log.trace("Instantiating {} view of type {} with backing bean type {}", me.viewName, getClass().getSimpleName(),
                implementedLayout.getClass().getSimpleName());
        addViewTitle();
    }

    protected void addViewTitle() {
        Label title = new Label(me.viewName);
        title.addStyleName(CustomTheme.VIEW_TITLE);
        addComponent(title);
    }

    @Override
    public final void enter(ViewChangeEvent event) {
        log.trace("Entering in view {} from view {} with parameters [{}]", me.viewName,
                Views.from(event.getOldView()).viewName, event.getParameters());
        onEnter(event);
    }

    protected abstract void onEnter(ViewChangeEvent event);

    public Views me() {
        return me;
    }

    /**
     * @return
     * @see com.vaadin.ui.HasComponents#iterator()
     */
    @Override
    public Iterator<Component> iterator() {
        return backingLayout.iterator();
    }

    /**
     * @param c
     * @see com.vaadin.ui.ComponentContainer#addComponent(com.vaadin.ui.Component)
     */
    @Override
    public void addComponent(Component c) {
        backingLayout.addComponent(c);
    }

    /**
     * @param components
     * @see com.vaadin.ui.ComponentContainer#addComponents(com.vaadin.ui.Component[])
     */
    @Override
    public void addComponents(Component... components) {
        backingLayout.addComponents(components);
    }

    /**
     * @return
     * @see com.vaadin.shared.Connector#getConnectorId()
     */
    @Override
    public String getConnectorId() {
        return backingLayout.getConnectorId();
    }

    /**
     * @param c
     * @see com.vaadin.ui.ComponentContainer#removeComponent(com.vaadin.ui.Component)
     */
    @Override
    public void removeComponent(Component c) {
        backingLayout.removeComponent(c);
    }

    /**
     * @param listener
     * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#addComponentAttachListener(com.vaadin.ui.HasComponents.ComponentAttachListener)
     */
    @Override
    public void addComponentAttachListener(ComponentAttachListener listener) {
        backingLayout.addComponentAttachListener(listener);
    }

    /**
     * @see com.vaadin.ui.ComponentContainer#removeAllComponents()
     */
    @Override
    public void removeAllComponents() {
        backingLayout.removeAllComponents();
    }

    /**
     * @param oldComponent
     * @param newComponent
     * @see com.vaadin.ui.ComponentContainer#replaceComponent(com.vaadin.ui.Component, com.vaadin.ui.Component)
     */
    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        backingLayout.replaceComponent(oldComponent, newComponent);
    }

    /**
     * @param listener
     * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#removeComponentAttachListener(com.vaadin.ui.HasComponents.ComponentAttachListener)
     */
    @Override
    public void removeComponentAttachListener(ComponentAttachListener listener) {
        backingLayout.removeComponentAttachListener(listener);
    }

    /**
     * @param listener
     * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#addComponentDetachListener(com.vaadin.ui.HasComponents.ComponentDetachListener)
     */
    @Override
    public void addComponentDetachListener(ComponentDetachListener listener) {
        backingLayout.addComponentDetachListener(listener);
    }

    /**
     * @param listener
     * @see com.vaadin.ui.HasComponents.ComponentAttachDetachNotifier#removeComponentDetachListener(com.vaadin.ui.HasComponents.ComponentDetachListener)
     */
    @Override
    public void removeComponentDetachListener(ComponentDetachListener listener) {
        backingLayout.removeComponentDetachListener(listener);
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getStyleName()
     */
    @Override
    public String getStyleName() {
        return backingLayout.getStyleName();
    }

    /**
     * @return
     * @deprecated
     * @see com.vaadin.ui.ComponentContainer#getComponentIterator()
     */
    @Deprecated
    @Override
    public Iterator<Component> getComponentIterator() {
        return backingLayout.getComponentIterator();
    }

    /**
     * @return
     * @see com.vaadin.ui.ComponentContainer#getComponentCount()
     */
    @Override
    public int getComponentCount() {
        return backingLayout.getComponentCount();
    }

    /**
     * @param listener
     * @see com.vaadin.server.ClientConnector#addAttachListener(com.vaadin.server.ClientConnector.AttachListener)
     */
    @Override
    public void addAttachListener(AttachListener listener) {
        backingLayout.addAttachListener(listener);
    }

    /**
     * @param listener
     * @see com.vaadin.server.ClientConnector#removeAttachListener(com.vaadin.server.ClientConnector.AttachListener)
     */
    @Override
    public void removeAttachListener(AttachListener listener) {
        backingLayout.removeAttachListener(listener);
    }

    /**
     * @param style
     * @see com.vaadin.ui.Component#setStyleName(java.lang.String)
     */
    @Override
    public void setStyleName(String style) {
        backingLayout.setStyleName(style);
    }

    /**
     * @param listener
     * @see com.vaadin.server.ClientConnector#addDetachListener(com.vaadin.server.ClientConnector.DetachListener)
     */
    @Override
    public void addDetachListener(DetachListener listener) {
        backingLayout.addDetachListener(listener);
    }

    /**
     * @param source
     * @see com.vaadin.ui.ComponentContainer#moveComponentsFrom(com.vaadin.ui.ComponentContainer)
     */
    @Override
    public void moveComponentsFrom(ComponentContainer source) {
        backingLayout.moveComponentsFrom(source);
    }

    /**
     * @param listener
     * @see com.vaadin.server.ClientConnector#removeDetachListener(com.vaadin.server.ClientConnector.DetachListener)
     */
    @Override
    public void removeDetachListener(DetachListener listener) {
        backingLayout.removeDetachListener(listener);
    }

    /**
     * @param listener
     * @deprecated
     * @see com.vaadin.ui.ComponentContainer#addListener(com.vaadin.ui.HasComponents.ComponentAttachListener)
     */
    @Deprecated
    @Override
    public void addListener(ComponentAttachListener listener) {
        backingLayout.addListener(listener);
    }

    /**
     * @param listener
     * @deprecated
     * @see com.vaadin.ui.ComponentContainer#removeListener(com.vaadin.ui.HasComponents.ComponentAttachListener)
     */
    @Deprecated
    @Override
    public void removeListener(ComponentAttachListener listener) {
        backingLayout.removeListener(listener);
    }

    /**
     * @return
     * @see com.vaadin.server.Sizeable#getWidth()
     */
    @Override
    public float getWidth() {
        return backingLayout.getWidth();
    }

    /**
     * @param listener
     * @deprecated
     * @see com.vaadin.ui.ComponentContainer#addListener(com.vaadin.ui.HasComponents.ComponentDetachListener)
     */
    @Deprecated
    @Override
    public void addListener(ComponentDetachListener listener) {
        backingLayout.addListener(listener);
    }

    /**
     * @return
     * @see com.vaadin.server.ClientConnector#retrievePendingRpcCalls()
     */
    @Override
    public List<ClientMethodInvocation> retrievePendingRpcCalls() {
        return backingLayout.retrievePendingRpcCalls();
    }

    /**
     * @return
     * @see com.vaadin.server.Sizeable#getHeight()
     */
    @Override
    public float getHeight() {
        return backingLayout.getHeight();
    }

    /**
     * @param listener
     * @deprecated
     * @see com.vaadin.ui.ComponentContainer#removeListener(com.vaadin.ui.HasComponents.ComponentDetachListener)
     */
    @Deprecated
    @Override
    public void removeListener(ComponentDetachListener listener) {
        backingLayout.removeListener(listener);
    }

    /**
     * @return
     * @see com.vaadin.server.ClientConnector#isConnectorEnabled()
     */
    @Override
    public boolean isConnectorEnabled() {
        return backingLayout.isConnectorEnabled();
    }

    /**
     * @return
     * @see com.vaadin.server.Sizeable#getWidthUnits()
     */
    @Override
    public Unit getWidthUnits() {
        return backingLayout.getWidthUnits();
    }

    /**
     * @return
     * @see com.vaadin.server.Sizeable#getHeightUnits()
     */
    @Override
    public Unit getHeightUnits() {
        return backingLayout.getHeightUnits();
    }

    /**
     * @return
     * @see com.vaadin.server.ClientConnector#getStateType()
     */
    @Override
    public Class<? extends SharedState> getStateType() {
        return backingLayout.getStateType();
    }

    /**
     * @param height
     * @see com.vaadin.server.Sizeable#setHeight(java.lang.String)
     */
    @Override
    public void setHeight(String height) {
        backingLayout.setHeight(height);
    }

    /**
     * @deprecated
     * @see com.vaadin.server.ClientConnector#requestRepaint()
     */
    @Deprecated
    @Override
    public void requestRepaint() {
        backingLayout.requestRepaint();
    }

    /**
     * @param style
     * @see com.vaadin.ui.Component#addStyleName(java.lang.String)
     */
    @Override
    public void addStyleName(String style) {
        backingLayout.addStyleName(style);
    }

    /**
     * @see com.vaadin.server.ClientConnector#markAsDirty()
     */
    @Override
    public void markAsDirty() {
        backingLayout.markAsDirty();
    }

    /**
     * @param width
     * @param unit
     * @see com.vaadin.server.Sizeable#setWidth(float, com.vaadin.server.Sizeable.Unit)
     */
    @Override
    public void setWidth(float width, Unit unit) {
        backingLayout.setWidth(width, unit);
    }

    /**
     * @deprecated
     * @see com.vaadin.server.ClientConnector#requestRepaintAll()
     */
    @Deprecated
    @Override
    public void requestRepaintAll() {
        backingLayout.requestRepaintAll();
    }

    /**
     * @see com.vaadin.server.ClientConnector#markAsDirtyRecursive()
     */
    @Override
    public void markAsDirtyRecursive() {
        backingLayout.markAsDirtyRecursive();
    }

    /**
     * @param height
     * @param unit
     * @see com.vaadin.server.Sizeable#setHeight(float, com.vaadin.server.Sizeable.Unit)
     */
    @Override
    public void setHeight(float height, Unit unit) {
        backingLayout.setHeight(height, unit);
    }

    /**
     * @param width
     * @see com.vaadin.server.Sizeable#setWidth(java.lang.String)
     */
    @Override
    public void setWidth(String width) {
        backingLayout.setWidth(width);
    }

    /**
     * @param style
     * @see com.vaadin.ui.Component#removeStyleName(java.lang.String)
     */
    @Override
    public void removeStyleName(String style) {
        backingLayout.removeStyleName(style);
    }

    /**
     * @see com.vaadin.server.Sizeable#setSizeFull()
     */
    @Override
    public void setSizeFull() {
        backingLayout.setSizeFull();
    }

    /**
     * @see com.vaadin.server.Sizeable#setSizeUndefined()
     */
    @Override
    public void setSizeUndefined() {
        backingLayout.setSizeUndefined();
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getPrimaryStyleName()
     */
    @Override
    public String getPrimaryStyleName() {
        return backingLayout.getPrimaryStyleName();
    }

    /**
     * @param style
     * @see com.vaadin.ui.Component#setPrimaryStyleName(java.lang.String)
     */
    @Override
    public void setPrimaryStyleName(String style) {
        backingLayout.setPrimaryStyleName(style);
    }

    /**
     * @see com.vaadin.server.ClientConnector#detach()
     */
    @Override
    public void detach() {
        backingLayout.detach();
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return backingLayout.isEnabled();
    }

    /**
     * @return
     * @see com.vaadin.server.ClientConnector#getExtensions()
     */
    @Override
    public Collection<Extension> getExtensions() {
        return backingLayout.getExtensions();
    }

    /**
     * @param extension
     * @see com.vaadin.server.ClientConnector#removeExtension(com.vaadin.server.Extension)
     */
    @Override
    public void removeExtension(Extension extension) {
        backingLayout.removeExtension(extension);
    }

    /**
     * @param initial
     * @see com.vaadin.server.ClientConnector#beforeClientResponse(boolean)
     */
    @Override
    public void beforeClientResponse(boolean initial) {
        backingLayout.beforeClientResponse(initial);
    }

    /**
     * @param enabled
     * @see com.vaadin.ui.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        backingLayout.setEnabled(enabled);
    }

    /**
     * @return
     * @throws JSONException
     * @see com.vaadin.server.ClientConnector#encodeState()
     */
    @Override
    public JSONObject encodeState() throws JSONException {
        return backingLayout.encodeState();
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#isVisible()
     */
    @Override
    public boolean isVisible() {
        return backingLayout.isVisible();
    }

    /**
     * @param request
     * @param response
     * @param path
     * @return
     * @throws IOException
     * @see com.vaadin.server.ClientConnector#handleConnectorRequest(com.vaadin.server.VaadinRequest,
     *      com.vaadin.server.VaadinResponse, java.lang.String)
     */
    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
        return backingLayout.handleConnectorRequest(request, response, path);
    }

    /**
     * @param visible
     * @see com.vaadin.ui.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        backingLayout.setVisible(visible);
    }

    /**
     * @param rpcInterfaceName
     * @return
     * @see com.vaadin.server.ClientConnector#getRpcManager(java.lang.String)
     */
    @Override
    public ServerRpcManager<?> getRpcManager(String rpcInterfaceName) {
        return backingLayout.getRpcManager(rpcInterfaceName);
    }

    /**
     * @return
     * @see com.vaadin.server.ClientConnector#getErrorHandler()
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return backingLayout.getErrorHandler();
    }

    /**
     * @param errorHandler
     * @see com.vaadin.server.ClientConnector#setErrorHandler(com.vaadin.server.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        backingLayout.setErrorHandler(errorHandler);
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getParent()
     */
    @Override
    public HasComponents getParent() {
        return backingLayout.getParent();
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return backingLayout.isReadOnly();
    }

    /**
     * @param readOnly
     * @see com.vaadin.ui.Component#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        backingLayout.setReadOnly(readOnly);
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getCaption()
     */
    @Override
    public String getCaption() {
        return backingLayout.getCaption();
    }

    /**
     * @param caption
     * @see com.vaadin.ui.Component#setCaption(java.lang.String)
     */
    @Override
    public void setCaption(String caption) {
        backingLayout.setCaption(caption);
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getIcon()
     */
    @Override
    public Resource getIcon() {
        return backingLayout.getIcon();
    }

    /**
     * @param icon
     * @see com.vaadin.ui.Component#setIcon(com.vaadin.server.Resource)
     */
    @Override
    public void setIcon(Resource icon) {
        backingLayout.setIcon(icon);
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getUI()
     */
    @Override
    public UI getUI() {
        return backingLayout.getUI();
    }

    /**
     * @see com.vaadin.ui.Component#attach()
     */
    @Override
    public void attach() {
        backingLayout.attach();
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getLocale()
     */
    @Override
    public Locale getLocale() {
        return backingLayout.getLocale();
    }

    /**
     * @param id
     * @see com.vaadin.ui.Component#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        backingLayout.setId(id);
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getId()
     */
    @Override
    public String getId() {
        return backingLayout.getId();
    }

    /**
     * @return
     * @see com.vaadin.ui.Component#getDescription()
     */
    @Override
    public String getDescription() {
        return backingLayout.getDescription();
    }

    /**
     * @param listener
     * @see com.vaadin.ui.Component#addListener(com.vaadin.ui.Component.Listener)
     */
    @Override
    public void addListener(Listener listener) {
        backingLayout.addListener(listener);
    }

    /**
     * @param listener
     * @see com.vaadin.ui.Component#removeListener(com.vaadin.ui.Component.Listener)
     */
    @Override
    public void removeListener(Listener listener) {
        backingLayout.removeListener(listener);
    }

    @Override
    public void setParent(HasComponents parent) {
        backingLayout.setParent(parent);

    }

    @Override
    public boolean isAttached() {
        return backingLayout.isAttached();
    }

}
