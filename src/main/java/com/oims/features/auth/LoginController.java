package com.oims.features.auth;

import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.core.dao.UserDao;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    public static void handleLogin(String username, String password, String role) {
        AlertMessage alert = new AlertMessage();
        if(role.isEmpty() || username.isEmpty() || password.isEmpty()){
            alert.errorMessage("Hãy điền đầy đủ các trường!");
        }else{
            Optional<User> authenticatedUser = authenticate(username, password, role);

            if (authenticatedUser.isPresent()) {
                User currentUser = authenticatedUser.get();
                AppSession.getInstance().setCurrentUser(currentUser);

            } else {
                alert.errorMessage("Sai tên đăng nhập, mật khẩu hoặc vai trò!");
            }
        }
    }

    private static Optional<User> authenticate(String username, String password, String roleLabel) {
        UserRole role = resolveRole(roleLabel);
        if (role == null || username == null || password == null) {
            return Optional.empty();
        }

        try {
            return new UserDao().findByCredentials(username.trim(), password, role);
        } catch (SQLException exception) {
            throw new RuntimeException("Không thể kiểm tra thông tin đăng nhập", exception);
        }
    }

    private static UserRole resolveRole(String roleLabel) {
        if (roleLabel == null) {
            return null;
        }

        return switch (roleLabel.trim()) {
            case "Admin" -> UserRole.ADMIN;
            case "NV.BP.Bán hàng" -> UserRole.SALES;
            case "NV.BP.Đặt hàng" -> UserRole.OVERSEAS_ORDER;
            case "NV.BP.Quản lý kho" -> UserRole.WAREHOUSE;
            default -> null;
        };
    }
}
