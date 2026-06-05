package com.oims.features.sales_requests.edit;

import com.oims.core.dao.DaoFactory;

public final class EditServiceFactory {

    private static ISalesRequestEditPolicy editPolicy = new DefaultSalesRequestEditPolicy();
    private static IEditRequestService editRequestService = new EditRequestService(
            DaoFactory.getMerchandiseDao(),
            DaoFactory.getSalesRequestDao(),
            DaoFactory.getSalesRequestItemDao(),
            DaoFactory.getUserDao(),
            editPolicy
    );

    private EditServiceFactory() {
    }

    public static ISalesRequestEditPolicy getEditPolicy() {
        return editPolicy;
    }

    public static void setEditPolicy(ISalesRequestEditPolicy policy) {
        editPolicy = policy;
        resetEditRequestService();
    }

    public static IEditRequestService getEditRequestService() {
        return editRequestService;
    }

    public static void setEditRequestService(IEditRequestService service) {
        editRequestService = service;
    }

    private static void resetEditRequestService() {
        editRequestService = new EditRequestService(
                DaoFactory.getMerchandiseDao(),
                DaoFactory.getSalesRequestDao(),
                DaoFactory.getSalesRequestItemDao(),
                DaoFactory.getUserDao(),
                editPolicy
        );
    }
}
