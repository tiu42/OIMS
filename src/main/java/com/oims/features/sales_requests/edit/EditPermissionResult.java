package com.oims.features.sales_requests.edit;

import com.oims.core.model.SalesRequestStatus;

public record EditPermissionResult(boolean canEdit, String blockedMessage, SalesRequestStatus status) {

    public static EditPermissionResult allowed(SalesRequestStatus status) {
        return new EditPermissionResult(true, "", status);
    }

    public static EditPermissionResult denied(SalesRequestStatus status, String message) {
        return new EditPermissionResult(false, message, status);
    }

    public static EditPermissionResult notFound() {
        return new EditPermissionResult(false, "Yêu cầu nhập hàng không tồn tại.", null);
    }
}
