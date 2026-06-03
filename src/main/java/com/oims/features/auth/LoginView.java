package com.oims.features.auth;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.oims.views.AdminView;
import com.oims.views.MainView;
import com.oims.views.OverseasView;
import com.oims.views.SalesView;
import com.oims.views.WarehouseView;
import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.core.session.AppSession;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginView implements Initializable {
    public TextField login_username;
    public PasswordField login_password;
    public ComboBox<String> login_userrole;
    public Button login_btn;

    private Stage primaryStage;

    public void handleSelectRole(ActionEvent actionEvent) {
    }

    public void handleLogin(ActionEvent actionEvent) {
        String username = login_username.getText() == null ? "" : login_username.getText().trim();
        String password = login_password.getText() == null ? "" : login_password.getText();
        String role = login_userrole.getValue() == null? "" : login_userrole.getValue().trim();

        LoginController.handleLogin(username, password, role);

        if(AppSession.getInstance().getCurrentUser() != null){
            primaryStage = (Stage) (((Node)actionEvent.getSource()).getScene().getWindow());
            try {
                switchToMainScene();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void switchToMainScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oims/main-view.fxml"));
        loader.setController(createMainViewController());
        Parent root = loader.load();

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root); // Thay đổi root giúp tối ưu hiệu năng hơn tạo Scene mới
        }

        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private MainView createMainViewController() {
        User currentUser = AppSession.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            throw new IllegalStateException("Không thể xác định vai trò người dùng hiện tại");
        }

        UserRole role = currentUser.getRole();
        return switch (role) {
            case ADMIN -> new AdminView();
            case SALES -> new SalesView();
            case OVERSEAS_ORDER -> new OverseasView();
            case WAREHOUSE -> new WarehouseView();
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<String> listU = new ArrayList<>();

        listU.add("Admin");
        listU.add("NV.BP.Bán hàng");
        listU.add("NV.BP.Đặt hàng");
        listU.add("NV.BP.Quản lý kho");

        ObservableList<String> listData = FXCollections.observableArrayList(listU);
        login_userrole.setItems(listData);
    }
}
