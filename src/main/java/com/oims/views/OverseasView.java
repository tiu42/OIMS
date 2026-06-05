package com.oims.views;

import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;

import java.sql.Struct;
import java.util.List;

public final class OverseasView extends MainView {

    @Override
    protected void renderSidebar() {
        setSidebar(createSidebar(List.of(
                section("Quản lý yêu cầu nhập hàng",
                        item("Danh sách yêu cầu", "/com/oims/features/sales_requests/list-sales-request-view.fxml",()->true),
                        item("Xử lý yêu cầu", "/com/oims/features/sales_requests/process-sales-request-view.fxml",this::check_session_req_for_process)
                ),
                section("Quản lý đơn hàng",
                        item("Danh sách đơn hàng đã tạo", "/com/oims/features/purchase_order/filtered-purchase-order-view.fxml",()->true),
                        item("Xem chi tiết đơn hàng", "/com/oims/features/purchase_order/detail-purchase-order-view.fxml",this::check_session_order),
                        item("Xử lý đơn hàng bị hủy", "/com/oims/features/purchase_order/process-canceled-purchase-order-view.fxml",this::check_session_order_for_canceled)
                ),
                section("Quản lý đối tác",
                        item("Danh sách site đối tác", "/com/oims/features/site/list-site-view.fxml",()->true),
                        item("Xem chi tiết site", "/com/oims/features/site/detail-site-view.fxml",this::check_session_site),
                        item("Thêm site đối tác", "/com/oims/features/site/create-site-view.fxml",this::check_session_site)
                ))));
    }

    private final AlertMessage alertMessage = new AlertMessage();

    boolean check_session_req_for_process(){
        if (AppSession.getInstance().getSelectedRequestId()==null){
            alertMessage.errorMessage("Chưa có yêu cầu nào được chọn! Hãy chọn 1 yêu cầu từ danh sách.");
            return false;
        }
        try {
            java.util.Optional<com.oims.core.model.SalesRequest> reqOpt = new com.oims.core.dao.SalesRequestDao().findById(AppSession.getInstance().getSelectedRequestId());
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

    boolean check_session_order(){
        if (AppSession.getInstance().getSelectedOrderId()==null){
            alertMessage.errorMessage("Chưa có đơn hàng nào được chọn! Hãy chọn 1 đơn hàng từ danh sách.");
            return false;
        }
        return true;
    }

    boolean check_session_order_for_canceled(){
        Integer orderId = AppSession.getInstance().getSelectedOrderId();
        if (orderId == null){
            alertMessage.errorMessage("Chưa có đơn hàng nào được chọn! Hãy chọn 1 đơn hàng từ danh sách.");
            return false;
        }
        try {
            java.util.Optional<com.oims.core.model.PurchaseOrder> orderOpt = new com.oims.core.dao.PurchaseOrderDao().findById(orderId);
            if (orderOpt.isPresent()) {
                com.oims.core.model.PurchaseOrderStatus status = orderOpt.get().getStatus();
                if (status != com.oims.core.model.PurchaseOrderStatus.CANCELLED) {
                    alertMessage.errorMessage("Đơn hàng được chọn không ở trạng thái bị hủy!");
                    return false;
                }
            } else {
                alertMessage.errorMessage("Đơn hàng không tồn tại.");
                return false;
            }
        } catch (java.sql.SQLException e) {
            alertMessage.errorMessage("Lỗi cơ sở dữ liệu: " + e.getMessage());
            return false;
        }
        return true;
    }

    boolean check_session_site(){
        if (AppSession.getInstance().getSelectedSiteId()==null){
            alertMessage.errorMessage("Chưa có site nào được chọn! Hãy chọn 1 site từ danh sách.");
            return false;
        }
        return true;
    }

}