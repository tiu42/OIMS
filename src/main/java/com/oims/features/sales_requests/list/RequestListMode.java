package com.oims.features.sales_requests.list;

import com.oims.core.model.User;
import com.oims.core.model.UserRole;

public enum RequestListMode {
    OVERSEAS(
            "Danh sách yêu cầu nhập hàng",
            "Quản lý, theo dõi và xử lý toàn bộ yêu cầu nhập hàng.",
            "Tìm theo request ID, người tạo hoặc trạng thái"
    ),
    SALES(
            "Danh sách yêu cầu nhập hàng cá nhân",
            "Xem, tạo và chỉnh sửa các yêu cầu do bạn tạo.",
            "Tìm theo request ID, ngày tạo hoặc trạng thái"
    );

    private final String title;
    private final String subtitle;
    private final String searchPrompt;

    RequestListMode(String title, String subtitle, String searchPrompt) {
        this.title = title;
        this.subtitle = subtitle;
        this.searchPrompt = searchPrompt;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getSearchPrompt() {
        return searchPrompt;
    }

    public static RequestListMode fromCurrentUser(User user) {
        if (user == null || user.getRole() == null) {
            throw new IllegalStateException("Không thể xác định chế độ hiển thị yêu cầu");
        }

        UserRole role = user.getRole();
        return switch (role) {
            case OVERSEAS_ORDER -> OVERSEAS;
            case SALES -> SALES;
            default -> throw new IllegalStateException("Vai trò không được hỗ trợ cho màn hình yêu cầu: " + role);
        };
    }
}
