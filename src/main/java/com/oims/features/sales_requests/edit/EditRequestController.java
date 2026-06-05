package com.oims.features.sales_requests.edit;

import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;
import com.oims.features.sales_requests.dto.RequestItemDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EditRequestController {

    private final IEditRequestService editRequestService;
    private final ISalesRequestEditPolicy editPolicy;

    public EditRequestController() {
        this(EditServiceFactory.getEditRequestService(), EditServiceFactory.getEditPolicy());
    }

    public EditRequestController(IEditRequestService editRequestService, ISalesRequestEditPolicy editPolicy) {
        this.editRequestService = editRequestService;
        this.editPolicy = editPolicy;
    }

    public EditPermissionResult checkEditPermission(int requestId) throws SQLException {
        return editRequestService.checkEditPermission(requestId);
    }

    public Optional<EditFormData> loadEditFormData(int requestId) throws SQLException {
        return editRequestService.loadEditFormData(requestId);
    }

    public void updateSalesRequest(int requestId, User modifier, List<RequestItemDTO> items) throws SQLException {
        editRequestService.updateSalesRequest(requestId, modifier, items);
    }

    public boolean isEditable(SalesRequestStatus status) {
        return editPolicy.isEditable(status);
    }

    public String getBlockedMessage(SalesRequestStatus status) {
        return editPolicy.getBlockedMessage(status);
    }
}
