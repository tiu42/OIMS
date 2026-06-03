package com.oims.features.sales_requests.edit;

import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.User;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.features.sales_requests.create.CreateRequestController.TempRequestItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestId = AppSession.getInstance().getSelectedRequestId();
        if (requestId == null) {
            alertMessage.errorMessage("Không tìm thấy mã yêu cầu cần chỉnh sửa.");
            navigateBackToDetail();
            return;
        }

        initMetadataAndTableData();
        initMerchComboBox();
        initTableColumns();
        initEventHandlers();

        addedItemsTable.setItems(addedItemsList);
        desiredDatePicker.setValue(LocalDate.now());
    }

    private void initMetadataAndTableData() {
        try {
            // Load SalesRequest
            var requestOpt = controller.getSalesRequest(requestId);
            if (requestOpt.isEmpty()) {
                alertMessage.errorMessage("Yêu cầu nhập hàng không tồn tại hoặc đã bị xóa.");
                navigateBackToDetail();
                return;
            }

            SalesRequest request = requestOpt.get();
            requestIdLabel.setText(String.valueOf(request.getRequestId()));
            creatorNameLabel.setText(controller.getCreatorName(request.getCreatedBy()));
            creationDateLabel.setText(request.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Format status label
            if (request.getStatus() != null) {
                statusLabel.setText(switch (request.getStatus()) {
                    case PENDING -> "Chờ xử lý";
                    case PROCESSING -> "Đang xử lý";
                    case COMPLETED -> "Hoàn tất";
                    case ERROR -> "Lỗi";
                });
            }

            // Load SalesRequestItems and populate addedItemsList
            List<SalesRequestItem> items = controller.getSalesRequestItems(requestId);
            List<Merchandise> merchandises = controller.getAllMerchandises();
            Map<String, String> merchNameMap = merchandises.stream()
                    .collect(Collectors.toMap(Merchandise::getMerchandiseCode, Merchandise::getMerchandiseName));

            for (SalesRequestItem item : items) {
                String name = merchNameMap.getOrDefault(item.getMerchandiseCode(), "Không xác định");
                addedItemsList.add(new DisplayRow(
                        item.getMerchandiseCode(),
                        name,
                        item.getQuantityOrdered(),
                        item.getUnit(),
                        item.getDesiredDeliveryDate()
                ));
            }

        } catch (SQLException e) {
            alertMessage.errorMessage("Không thể tải thông tin yêu cầu: " + e.getMessage());
            navigateBackToDetail();
        }
    }

    private void initMerchComboBox() {
        try {
            List<Merchandise> merchandises = controller.getAllMerchandises();
            merchComboBox.setItems(FXCollections.observableArrayList(merchandises));

            // Set how items are displayed
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

            // When merchandise is selected, pre-fill unit field
            merchComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    unitField.setText(newVal.getDefaultUnit());
                } else {
                    unitField.clear();
                }
            });

        } catch (SQLException e) {
            alertMessage.errorMessage("Không thể tải danh sách mặt hàng: " + e.getMessage());
        }
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

    private void handleAddItem() {
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

        if (unit == null || unit.isBlank()) {
            alertMessage.errorMessage("Đơn vị mặt hàng không được để trống.");
            return;
        }

        if (date == null) {
            alertMessage.errorMessage("Vui lòng chọn ngày nhận mong muốn.");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            alertMessage.errorMessage("Ngày nhận mong muốn không được ở trong quá khứ.");
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
        if (addedItemsList.isEmpty()) {
            alertMessage.errorMessage("Danh sách mặt hàng yêu cầu không được để trống.");
            return;
        }

        User currentUser = AppSession.getInstance().getCurrentUser();
        List<TempRequestItem> tempItems = new ArrayList<>();
        for (DisplayRow row : addedItemsList) {
            tempItems.add(new TempRequestItem(
                    row.getCode(),
                    row.getQuantity(),
                    row.getUnit(),
                    row.getDate()
            ));
        }

        try {
            controller.updateSalesRequest(requestId, currentUser, tempItems);
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
