package com.oims.views;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.core.session.AppSession;

import com.oims.core.util.AlertMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class MainView implements Initializable {

    @FXML
    protected StackPane contentArea;

    @FXML
    protected Button logoutButton;

    @FXML
    protected Label userNameLabel;

    @FXML
    protected Label userRoleLabel;

    @FXML
    protected VBox sidebarMenuContainer;

    @FXML
    protected Label welcomeLabel;

    @Override
    public final void initialize(URL url, ResourceBundle resourceBundle) {
        bindUserInfo();
        renderSidebar();
        logoutButton.setOnAction(this::handleLogout);
    }

    protected abstract void renderSidebar();

    protected final User getCurrentUser() {
        return AppSession.getInstance().getCurrentUser();
    }

    protected final void setSidebar(Node sidebar) {
        sidebarMenuContainer.getChildren().setAll(sidebar);
    }

    protected final void showContent(String viewPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException exception) {
            throw new RuntimeException("Không thể tải màn hình: " + viewPath, exception);
        }
    }

    protected final Node createSidebar(List<SidebarSection> sections) {
        Accordion accordion = new Accordion();
        accordion.setMaxWidth(Double.MAX_VALUE);

        for (SidebarSection section : sections) {
            if (!section.items().isEmpty()) {
                accordion.getPanes().add(createSection(section));
            }
        }

        if (accordion.getPanes().isEmpty()) {
            Label emptyLabel = new Label("Chưa có menu phù hợp");
            emptyLabel.getStyleClass().add("sidebar-empty-state");

            VBox emptyState = new VBox(emptyLabel);
            emptyState.setAlignment(Pos.CENTER_LEFT);
            emptyState.setPadding(new Insets(8.0, 0.0, 0.0, 0.0));
            return emptyState;
        }

        return accordion;
    }

    protected final SidebarSection section(String title, SidebarItem... items) {
        return new SidebarSection(title, List.of(items));
    }

    protected final SidebarItem item(String label, String viewPath, Supplier<Boolean> check) {
        return new SidebarItem(label, viewPath, check);
    }

    private TitledPane createSection(SidebarSection section) {
        VBox sectionContent = new VBox(8.0);
        sectionContent.setFillWidth(true);

        for (SidebarItem item : section.items()) {
            Button button = new Button(item.label());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("sidebar-action-button");
            button.setOnAction(event -> {if(item.check().get()){showContent(item.viewPath());}});
            sectionContent.getChildren().add(button);
        }

        TitledPane pane = new TitledPane(section.title(), sectionContent);
        pane.setAnimated(true);
        pane.setExpanded(false);
        return pane;
    }

    private void bindUserInfo() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName() == null || currentUser.getFullName().isBlank()
                    ? currentUser.getUsername()
                    : currentUser.getFullName());

            userRoleLabel.setText(formatRole(currentUser.getRole()));
            welcomeLabel.setText("Xin chào " + (currentUser.getFullName() == null || currentUser.getFullName().isBlank()
                    ? currentUser.getUsername()
                    : currentUser.getFullName()));
            return;
        }

        userNameLabel.setText("Chưa đăng nhập");
        userRoleLabel.setText("Vai trò không xác định");
    }

    private void handleLogout(ActionEvent event) {
        AppSession.getInstance().clear();

        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oims/features/auth/login-view.fxml"));
            Parent root = loader.load();

            Scene scene = stage.getScene();
            if (scene == null) {
                stage.setScene(new Scene(root));
            } else {
                scene.setRoot(root);
            }

            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException exception) {
            throw new RuntimeException("Không thể đăng xuất", exception);
        }
    }

    private String formatRole(UserRole role) {
        if (role == null) {
            return "Vai trò không xác định";
        }

        return role.toString();
    }

    public record SidebarSection(String title, List<SidebarItem> items) {
        public SidebarSection {
            items = List.copyOf(items);
        }
    }

    public record SidebarItem(String label, String viewPath, Supplier<Boolean> check) {
    }
}
