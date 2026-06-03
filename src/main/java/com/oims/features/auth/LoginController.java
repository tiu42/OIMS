package com.oims.features.auth;

import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.core.model.User;

import java.util.Optional;

public class LoginController {

    public static void handleLogin(String username, String password, String role) {
        AlertMessage alert = new AlertMessage();
        if(role.isEmpty() || username.isEmpty() || password.isEmpty()){
            alert.errorMessage("Hãy điền đầy đủ các trường!");
        }else{
            Optional<User> authenticatedUser = User.authenticate(username, password, role);

            if (authenticatedUser.isPresent()) {
                User currentUser = authenticatedUser.get();
                AppSession.getInstance().setCurrentUser(currentUser);

            } else {
                alert.errorMessage("Sai tên đăng nhập, mật khẩu hoặc vai trò!");
            }

        }
    }
}
