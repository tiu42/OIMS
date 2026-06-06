package com.oims.views;

import java.util.List;

public final class AdminView extends MainView {

    @Override
    protected void renderSidebar() {
        setSidebar(createSidebar(List.of(
                section("Quản lý người dùng",
                        item("Danh sách người dùng", "/com/oims/features/users/list-user-view.fxml", ()->true),
                        item("Xem chi tiết người dùng", "/com/oims/features/users/detail-user-view.fxml", ()->true),
                        item("Tạo người dùng mới", "/com/oims/features/users/create-user-view.fxml", ()->true)
                ),
                section("Quản lý yêu cầu nhập hàng",
                        item("Danh sách yêu cầu", "/com/oims/features/sales_requests/list-sales-request-view.fxml", ()->true),
                        item("Danh sách yêu cầu đã tạo", "/com/oims/features/sales_requests/list-sales-request-view.fxml", ()->true),
                        item("Tạo yêu cầu mới", "/com/oims/features/sales_requests/create-sales-request-view.fxml", ()->true),
                        item("Xử lý yêu cầu", "/com/oims/features/sales_requests/process-sales-request-view.fxml", this::check_session_req_for_process)
                ),
                section("Quản lý mặt hàng",
                        item("Danh sách mặt hàng", "/com/oims/features/merchandise/list-merchandise-view.fxml", ()->true),
                        item("Xem chi tiết mặt hàng", "/com/oims/features/merchandise/detail-merchandise-view.fxml", ()->true),
                        item("Thêm mặt hàng mới", "/com/oims/features/merchandise/create-merchandise-view.fxml", ()->true)
                ),
                section("Quản lý đơn hàng",
                        item("Danh sách đơn hàng đã tạo", "/com/oims/features/purchase_order/filtered-purchase-order-view.fxml", ()->true),
                        item("Xem chi tiết đơn hàng", "/com/oims/features/purchase_order/detail-purchase-order-view.fxml", ()->true),
                        item("Xử lý đơn hàng bị hủy", "/com/oims/features/purchase_order/process-canceled-purchase-order-view.fxml", ()->true)
                ),
                section("Quản lý nhập kho",
                        item("Danh sách đơn hàng nhập kho", "/com/oims/features/warehouse/list-purchase-order-view.fxml", ()->true),
                        item("Chi tiết đơn hàng nhập kho", "/com/oims/features/warehouse/detail-purchase-order-2-view.fxml", ()->true),
                        item("Xác nhận đơn hàng nhập kho", "/com/oims/features/warehouse/process-purchase-order-view.fxml", ()->true)
                ),
                section("Quản lý đối tác",
                        item("Danh sách site đối tác", "/com/oims/features/site/list-site-view.fxml", ()->true),
                        item("Thêm site đối tác", "/com/oims/features/site/create-site-view.fxml", ()->true)
                ))));
    }

    private final com.oims.core.util.AlertMessage alertMessage = new com.oims.core.util.AlertMessage();

    private boolean check_session_req_for_process() {
        if (com.oims.core.session.AppSession.getInstance().getSelectedRequestId() == null) {
            alertMessage.errorMessage("Chưa có yêu cầu nào được chọn! Hãy chọn 1 yêu cầu từ danh sách.");
            return false;
        }
        try {
            java.util.Optional<com.oims.core.model.SalesRequest> reqOpt = new com.oims.core.dao.SalesRequestDao().findById(com.oims.core.session.AppSession.getInstance().getSelectedRequestId());
            if (reqOpt.isPresent()) {
                com.oims.core.model.SalesRequestStatus status = reqOpt.get().getStatus();
                if (status == com.oims.core.model.SalesRequestStatus.COMPLETED || status == com.oims.core.model.SalesRequestStatus.ERROR) {
                    alertMessage.errorMessage("Yêu cầu này đã được xử lý xong và không thể xử lý lại.");
                    return false;
                }
            }
        } catch (java.sql.SQLException e) {
            alertMessage.errorMessage("Lỗi cơ sở dữ liệu: " + e.getMessage());
            return false;
        }
        return true;
    }
}