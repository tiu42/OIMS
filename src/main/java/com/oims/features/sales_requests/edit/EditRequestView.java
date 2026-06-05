package com.oims.features.sales_requests.edit;

import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.features.sales_requests.dto.RequestItemDTO;
import com.oims.features.sales_requests.shared.RequestItemValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EditRequestView implements Initializable {

    @FXML
    private Label pageTitle;

    @FXML
    private Label requestIdLabel;

    @FXML
    private Label creatorNameLabel;

    @FXML
    private Label creationDateLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label lockWarningLabel;

    @FXML
    private ComboBox<Merchandise> merchComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField unitField;

    @FXML
    private DatePicker desiredDatePicker;

    @FXML
    private TableView<DisplayRow> addedItemsTable;

    @FXML
    private TableColumn<DisplayRow, String> merchCodeCol;

    @FXML
    private TableColumn<DisplayRow, String> merchNameCol;

    @FXML
    private TableColumn<DisplayRow, String> quantityCol;

    @FXML
    private TableColumn<DisplayRow, String> unitCol;

    @FXML
    private TableColumn<DisplayRow, String> desiredDateCol;

    @FXML
    private TableColumn<DisplayRow, Void> actionCol;

    @FXML
    private Button addItemBtn;

    @FXML
    private Button submitBtn;

    @FXML
    private Button cancelBtn;

    private final EditRequestController controller = new EditRequestController();
    private final ObservableList<DisplayRow> addedItemsList = FXCollections.observableArrayList();
    private final AlertMessage alertMessage = new AlertMessage();
    private Integer requestId;
    private Timeline statusCheckTimeline;
    private boolean editLocked;
    private boolean lockMessageShown;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestId = AppSession.getInstance().getSelectedRequestId();
        if (requestId == null) {
            alertMessage.errorMessage("Không tìm thấy mã yêu cầu cần chỉnh sửa.");
            navigateBackToDetail();
            return;
        }

        if (!loadFormData()) {
            return;
        }

        initTableColumns();
        initEventHandlers();

        addedItemsTable.setItems(addedItemsList);
        desiredDatePicker.setValue(LocalDate.now());
        startStatusPolling();
    }

    private boolean loadFormData() {
        try {
            var formDataOpt = controller.loadEditFormData(requestId);
            if (formDataOpt.isEmpty()) {
                alertMessage.errorMessage("Yêu cầu nhập hàng không tồn tại hoặc đã bị xóa.");
                navigateBackToDetail();
                return false;
            }

            EditFormData formData = formDataOpt.get();
            SalesRequest request = formData.salesRequest();

            requestIdLabel.setText(String.valueOf(request.getRequestId()));
            creatorNameLabel.setText(formData.creatorName());
            creationDateLabel.setText(request.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            applyEditAvailability(request.getStatus(), false);

            initMerchComboBox(formData.merchandises());

            Map<String, String> merchNameMap = formData.merchandises().stream()
                    .collect(Collectors.toMap(Merchandise::getMerchandiseCode, Merchandise::getMerchandiseName));

            for (SalesRequestItem item : formData.items()) {
                String name = merchNameMap.getOrDefault(item.getMerchandiseCode(), "Không xác định");
                addedItemsList.add(new DisplayRow(
                        item.getMerchandiseCode(),
                        name,
                        item.getQuantityOrdered(),
                        item.getUnit(),
                        item.getDesiredDeliveryDate()
                ));
            }

            return true;
        } catch (SQLException e) {
            alertMessage.errorMessage("Không thể tải thông tin yêu cầu: " + e.getMessage());
            navigateBackToDetail();
            return false;
        }
    }

    private void initMerchComboBox(List<Merchandise> merchandises) {
        merchComboBox.setItems(FXCollections.observableArrayList(merchandises));

        merchComboBox.setConverter(new StringConverter<Merchandise>() {
            @Override
            public String toString(Merchandise object) {
                return object == null ? "" : object.getMerchandiseCode() + " - " + object.getMerchandiseName();
            }

            @Override
            public Merchandise fromString(String string) {
                return null;
            }
        });

        merchComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                unitField.setText(newVal.getDefaultUnit());
            } else {
                unitField.clear();
            }
        });
    }

    private void initTableColumns() {
        merchCodeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        merchNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        quantityCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getQuantity().toString()));
        unitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnit()));
        desiredDateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDateDisplay()));

        // Action Column: Delete row button
        actionCol.setCellFactory(param -> new TableCell<DisplayRow, Void>() {
            private final Button deleteBtn = new Button("Xóa");
            {
                deleteBtn.getStyleClass().add("btn-delete-row");
                deleteBtn.setOnAction(event -> {
                    DisplayRow row = getTableView().getItems().get(getIndex());
                    addedItemsList.remove(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    private void initEventHandlers() {
        addItemBtn.setOnAction(event -> handleAddItem());
        submitBtn.setOnAction(event -> handleSubmitChanges());
        cancelBtn.setOnAction(event -> handleCancel());
    }

    private void startStatusPolling() {
        statusCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> refreshEditAvailability()));
        statusCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        statusCheckTimeline.play();
    }

    private void stopStatusPolling() {
        if (statusCheckTimeline != null) {
            statusCheckTimeline.stop();
        }
    }

    private void refreshEditAvailability() {
        try {
            EditPermissionResult permission = controller.checkEditPermission(requestId);
            if (permission.status() == null) {
                return;
            }
            applyEditAvailability(permission.status(), true);
        } catch (SQLException ignored) {
            // Giữ trạng thái hiện tại nếu tạm thời không đọc được DB.
        }
    }

    private void applyEditAvailability(SalesRequestStatus status, boolean notifyOnLock) {
        updateStatusLabel(status);

        if (controller.isEditable(status)) {
            editLocked = false;
            lockMessageShown = false;
            setEditControlsDisabled(false);
            if (lockWarningLabel != null) {
                lockWarningLabel.setText("");
                lockWarningLabel.setVisible(false);
                lockWarningLabel.setManaged(false);
            }
            return;
        }

        editLocked = true;
        setEditControlsDisabled(true);

        String message = controller.getBlockedMessage(status);
        if (lockWarningLabel != null) {
            lockWarningLabel.setText(message);
            lockWarningLabel.setVisible(true);
            lockWarningLabel.setManaged(true);
        }

        if (notifyOnLock && !lockMessageShown) {
            lockMessageShown = true;
            alertMessage.errorMessage(message);
        }
    }

    private void updateStatusLabel(SalesRequestStatus status) {
        if (status == null || statusLabel == null) {
            return;
        }
        statusLabel.setText(switch (status) {
            case PENDING -> "Chờ xử lý";
            case PROCESSING -> "Đang xử lý";
            case COMPLETED -> "Hoàn tất";
            case ERROR -> "Lỗi";
        });
    }

    private void setEditControlsDisabled(boolean disabled) {
        if (addItemBtn != null) {
            addItemBtn.setDisable(disabled);
        }
        if (submitBtn != null) {
            submitBtn.setDisable(disabled);
        }
        if (merchComboBox != null) {
            merchComboBox.setDisable(disabled);
        }
        if (quantityField != null) {
            quantityField.setDisable(disabled);
        }
        if (unitField != null) {
            unitField.setDisable(disabled);
        }
        if (desiredDatePicker != null) {
            desiredDatePicker.setDisable(disabled);
        }
        if (addedItemsTable != null) {
            addedItemsTable.setDisable(disabled);
        }
    }

    private void handleAddItem() {
        if (editLocked) {
            alertMessage.errorMessage("Yêu cầu không còn ở trạng thái có thể chỉnh sửa.");
            return;
        }

        Merchandise selectedMerch = merchComboBox.getValue();
        String qtyText = quantityField.getText();
        String unit = unitField.getText();
        LocalDate date = desiredDatePicker.getValue();

        if (selectedMerch == null) {
            alertMessage.errorMessage("Vui lòng chọn một mặt hàng.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText.trim());
            if (qty <= 0) {
                alertMessage.errorMessage("Số lượng nhập hàng phải lớn hơn 0.");
                return;
            }
        } catch (NumberFormatException e) {
            alertMessage.errorMessage("Số lượng nhập hàng phải là một số nguyên dương.");
            return;
        }

        try {
            RequestItemValidator.validateItem(new RequestItemDTO(
                    selectedMerch.getMerchandiseCode(),
                    qty,
                    unit,
                    date
            ));
        } catch (IllegalArgumentException e) {
            alertMessage.errorMessage(e.getMessage());
            return;
        }

        // Check if item already exists in table
        for (DisplayRow row : addedItemsList) {
            if (row.getCode().equals(selectedMerch.getMerchandiseCode())) {
                alertMessage.errorMessage("Mặt hàng này đã có trong danh sách yêu cầu.");
                return;
            }
        }

        addedItemsList.add(new DisplayRow(
                selectedMerch.getMerchandiseCode(),
                selectedMerch.getMerchandiseName(),
                qty,
                unit,
                date
        ));

        // Clear input form except date
        merchComboBox.setValue(null);
        quantityField.clear();
        unitField.clear();
    }

    private void handleSubmitChanges() {
        if (editLocked) {
            alertMessage.errorMessage("Yêu cầu không còn ở trạng thái có thể chỉnh sửa.");
            return;
        }

        if (addedItemsList.isEmpty()) {
            alertMessage.errorMessage("Danh sách mặt hàng yêu cầu không được để trống.");
            return;
        }

        User currentUser = AppSession.getInstance().getCurrentUser();
        List<RequestItemDTO> items = new ArrayList<>();
        for (DisplayRow row : addedItemsList) {
            items.add(new RequestItemDTO(
                    row.getCode(),
                    row.getQuantity(),
                    row.getUnit(),
                    row.getDate()
            ));
        }

        try {
            controller.updateSalesRequest(requestId, currentUser, items);
            alertMessage.successMessage("Cập nhật yêu cầu nhập hàng thành công.");
            navigateBackToDetail();
        } catch (SQLException | IllegalArgumentException e) {
            alertMessage.errorMessage("Cập nhật yêu cầu nhập hàng thất bại: " + e.getMessage());
        }
    }

    private void handleCancel() {
        navigateBackToDetail();
    }

    private void navigateBackToDetail() {
        stopStatusPolling();
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/detail-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            alertMessage.errorMessage("Không thể quay lại chi tiết yêu cầu: " + e.getMessage());
        }
    }

    public static class DisplayRow {
        private final String code;
        private final String name;
        private final Integer quantity;
        private final String unit;
        private final LocalDate date;

        public DisplayRow(String code, String name, Integer quantity, String unit, LocalDate date) {
            this.code = code;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.date = date;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public Integer getQuantity() { return quantity; }
        public String getUnit() { return unit; }
        public LocalDate getDate() { return date; }
        public String getDateDisplay() {
            return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }
}
