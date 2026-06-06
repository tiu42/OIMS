package com.oims.core.session;

import com.oims.core.model.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class AppSession {
    private static final AppSession INSTANCE = new AppSession();

    private User currentUser;
    private final ObjectProperty<Integer> selectedRequestId = new SimpleObjectProperty<>(null);

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void clear() {
        currentUser = null;
        clearSelectedMerchandise();
        clearSelectedOrder();
        clearSelectedSite();
        clearSelectedRequest();
    }

    public ObjectProperty<Integer> selectedRequestIdProperty() {
        return selectedRequestId;
    }

    public Integer getSelectedRequestId() {
        return selectedRequestId.get();
    }

    public void setSelectedRequestId(Integer id) {
        this.selectedRequestId.set(id);
    }

    public void clearSelectedRequest() {
        this.selectedRequestId.set(null);
    }

    private final ObjectProperty<Integer> selectedOrderId = new SimpleObjectProperty<>(null);
    public ObjectProperty<Integer> selectedOrderIdProperty() {
        return selectedOrderId;
    }

    public Integer getSelectedOrderId() {
        return selectedOrderId.get();
    }

    public void setSelectedOrderId(Integer id) {
        this.selectedOrderId.set(id);
    }

    public void clearSelectedOrder() {
        this.selectedOrderId.set(null);
    }

    private final ObjectProperty<String> selectedSiteCode = new SimpleObjectProperty<>(null);
    public ObjectProperty<String> selectedSiteCodeProperty() {
        return selectedSiteCode;
    }

    public String getSelectedSiteCode() {
        return selectedSiteCode.get();
    }

    public void setSelectedSiteCode(String siteCode) {
        this.selectedSiteCode.set(siteCode);
    }

    public void clearSelectedSite() {
        this.selectedSiteCode.set(null);
    }

    private final ObjectProperty<Integer> selectedMerchandiseId = new SimpleObjectProperty<>(null);
    public ObjectProperty<Integer> selectedMerchandiseIdProperty() {
        return selectedMerchandiseId;
    }

    public Integer getSelectedMerchandiseId() {
        return selectedMerchandiseId.get();
    }

    public void setSelectedMerchandiseId(Integer id) {
        this.selectedMerchandiseId.set(id);
    }

    public void clearSelectedMerchandise() {
        this.selectedMerchandiseId.set(null);
    }
}