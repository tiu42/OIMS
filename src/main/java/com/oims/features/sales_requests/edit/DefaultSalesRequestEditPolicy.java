package com.oims.features.sales_requests.edit;

import com.oims.core.model.SalesRequestStatus;

public class DefaultSalesRequestEditPolicy implements ISalesRequestEditPolicy {

    @Override
    public boolean isEditable(SalesRequestStatus status) {
        return status == SalesRequestStatus.PENDING;
    }

    @Override
    public String getBlockedMessage(SalesRequestStatus status) {
        if (status == null) {
            return "Không xác định trạng thái yêu cầu.";
        }
        return switch (status) {
            case PENDING -> "";
            case PROCESSING -> "Yêu cầu đang được bộ phận đặt hàng quốc tế xử lý. Không thể chỉnh sửa.";
            case COMPLETED -> "Yêu cầu đã được xử lý xong. Không thể chỉnh sửa.";
            case ERROR -> "Yêu cầu đã được xử lý (có lỗi). Không thể chỉnh sửa.";
        };
    }

    @Override
    public EditPermissionResult evaluate(SalesRequestStatus status) {
        if (status == null) {
            return EditPermissionResult.denied(null, getBlockedMessage(null));
        }
        if (isEditable(status)) {
            return EditPermissionResult.allowed(status);
        }
        return EditPermissionResult.denied(status, getBlockedMessage(status));
    }
}
