package com.oims.features.site.create;

import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CreateSiteView implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label pageTitle;

    @FXML
    private Label creatorNameLabel;

    @FXML
    private Label creationDateLabel;

    @FXML
    private TextField siteCodeField;

    @FXML
    private TextField siteNameField;

    @FXML
    private TextField countryField;

    @FXML
    private TextArea contactInfoArea;

    @FXML
    private Button submitBtn;

    @FXML
    private Button cancelBtn;

    private final CreateSiteController controller = new CreateSiteController();
    private final AlertMessage alertMessage = new AlertMessage();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMetadataCard();
        submitBtn.setOnAction(event -> handleSubmit());
        cancelBtn.setOnAction(event -> navigateToList());
    }

    private void initMetadataCard() {
        var currentUser = AppSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getFullName();
            if (displayName == null || displayName.isBlank()) {
                displayName = currentUser.getUsername();
            }
            creatorNameLabel.setText(displayName == null || displayName.isBlank() ? "Không xác định" : displayName);
        } else {
            creatorNameLabel.setText("Không xác định");
        }
        creationDateLabel.setText(LocalDate.now().format(DATE_FORMAT));
    }

    private void handleSubmit() {
        try {
            String error = controller.createSite(
                    siteCodeField.getText(),
                    siteNameField.getText(),
                    countryField.getText(),
                    contactInfoArea.getText()
            );

            if (error != null) {
                alertMessage.errorMessage(error);
                return;
            }

            String createdCode = siteCodeField.getText().trim().toUpperCase();
            AppSession.getInstance().setSelectedSiteCode(createdCode);
            alertMessage.successMessage("Đã thêm site đối tác \"" + createdCode + "\" thành công.");
            clearForm();
            navigateToList();
        } catch (SQLException exception) {
            alertMessage.errorMessage("Lỗi cơ sở dữ liệu: " + exception.getMessage());
        }
    }

    private void clearForm() {
        siteCodeField.clear();
        siteNameField.clear();
        countryField.clear();
        contactInfoArea.clear();
    }

    private void navigateToList() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/site/list-site-view.fxml"));
            StackPane contentArea = (StackPane) submitBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException exception) {
            alertMessage.errorMessage("Không thể quay lại danh sách site: " + exception.getMessage());
        }
    }
}
