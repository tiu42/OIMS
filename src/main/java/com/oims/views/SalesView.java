package com.oims.views;

import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;

import java.util.List;

public final class SalesView extends MainView {

    @Override
    protected void renderSidebar() {
        setSidebar(createSidebar(List.of(
                section("Quản lý yêu cầu nhập hàng",
                        item("Danh sách yêu cầu đã tạo", "/com/oims/features/sales_requests/list-sales-request-view.fxml", ()->true),
                        item("Tạo yêu cầu mới", "/com/oims/features/sales_requests/create-sales-request-view.fxml", () -> true)
                ),
                section("Quản lý mặt hàng",
                        item("Danh sách mặt hàng", "/com/oims/features/merchandise/list-merchandise-view.fxml", ()->true),
                        item("Xem chi tiết mặt hàng", "/com/oims/features/merchandise/detail-merchandise-view.fxml", this::check_session_merch),
                        item("Thêm mặt hàng mới", "/com/oims/features/merchandise/create-merchandise-view.fxml", ()-> true)
                ))));
    }

    private final AlertMessage alertMessage = new AlertMessage();

    boolean check_session_merch(){
        if (AppSession.getInstance().getSelectedMerchandiseId()==null){
            alertMessage.errorMessage("Chưa có mặt hàng nào được chọn! Hãy chọn 1 mặt hàng từ danh sách.");
            return false;
        }
        return true;
    }
}