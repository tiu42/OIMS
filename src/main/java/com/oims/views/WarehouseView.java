package com.oims.views;

import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;

import java.util.List;

public final class WarehouseView extends MainView {

    @Override
    protected void renderSidebar() {
        setSidebar(createSidebar(List.of(
                section("Quản lý nhập kho",
                        item("Danh sách đơn hàng nhập kho", "/com/oims/features/purchase_order/filtered-purchase-order-view.fxml", ()->true),
                        item("Chi tiết đơn hàng nhập kho", "/com/oims/features/purchase_order/detail-purchase-order-view.fxml",this::check_session_order),
                        item("Xác nhận đơn hàng nhập kho", "/com/oims/features/warehouse/process-purchase-order-view.fxml",this::check_session_order_for_confirm)
                ))));
    }
    private final AlertMessage alertMessage = new AlertMessage();

    boolean check_session_order(){
        if (AppSession.getInstance().getSelectedOrderId()==null){
            alertMessage.errorMessage("Chưa có đơn nào được chọn! Hãy chọn 1 đơn hàng từ danh sách.");
            return false;
        }
        return true;
    }

    boolean check_session_order_for_confirm(){
        Integer orderId = AppSession.getInstance().getSelectedOrderId();
        if (orderId == null){
            alertMessage.errorMessage("Chưa có đơn nào được chọn! Hãy chọn 1 đơn hàng từ danh sách.");
            return false;
        }
        try {
            java.util.Optional<com.oims.core.model.PurchaseOrder> orderOpt = new com.oims.core.dao.PurchaseOrderDao().findById(orderId);
            if (orderOpt.isPresent()) {
                com.oims.core.model.PurchaseOrderStatus status = orderOpt.get().getStatus();
                if (status != com.oims.core.model.PurchaseOrderStatus.SENT
                        && status != com.oims.core.model.PurchaseOrderStatus.CONFIRMED) {
                    alertMessage.errorMessage("Chỉ có thể xác nhận đơn hàng nhập kho ở trạng thái 'Chưa xác nhận'!");
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
}
