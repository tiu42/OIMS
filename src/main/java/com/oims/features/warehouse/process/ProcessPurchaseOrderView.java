package com.oims.features.warehouse.process;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProcessPurchaseOrderView implements Initializable {

    @FXML
    private Label pageTitle;

    @FXML
    private Label orderIdLabel;

    @FXML
    private Label siteNameLabel;

    @FXML
    private Label creatorNameLabel;

    @FXML
    private Label orderDateLabel;

    @FXML
    private Label deliveryLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TableView<ProcessItemRow> orderItemsTable;

    @FXML
    private TableColumn<ProcessItemRow, String> merchCodeCol;

    @FXML
    private TableColumn<ProcessItemRow, String> merchNameCol;

    @FXML
    private TableColumn<ProcessItemRow, String> quantityCol;

    @FXML
    private TableColumn<ProcessItemRow, String> unitCol;

    @FXML
    private TableColumn<ProcessItemRow, String> statusCol;

    @FXML
    private TableColumn<ProcessItemRow, String> shortageQtyCol;

    @FXML
    private Button backBtn;

    @FXML
    private Button approveBtn;

    private final ProcessPurchaseOrderController controller = new ProcessPurchaseOrderController();

    private final ObservableList<ProcessItemRow> itemsList = FXCollections.observableArrayList();
    private Integer orderId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = AppSession.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.WAREHOUSE) {
            new AlertMessage().errorMessage("Chỉ nhân viên quản lý kho mới được xác nhận đơn hàng nhập kho.");
            handleBack();
            return;
        }

        orderId = AppSession.getInstance().getSelectedOrderId();
        if (orderId == null) {
            new AlertMessage().errorMessage("Không tìm thấy mã đơn hàng cần duyệt.");
            handleBack();
            return;
        }

        initTableColumns();
        loadOrderData();
        initEventHandlers();

        orderItemsTable.setItems(itemsList);
    }

    private void initTableColumns() {
        merchCodeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMerchandiseCode()));
        merchNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMerchandiseName()));
        quantityCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getQuantityOrdered())));
        unitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnit()));

        // Custom Cell Factory for Received Status ComboBox
        statusCol.setCellFactory(col -> new TableCell<ProcessItemRow, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList("Đủ", "Thiếu"));
            {
                comboBox.setOnAction(e -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        ProcessItemRow row = getTableView().getItems().get(getIndex());
                        row.setStatus(comboBox.getValue());
                        if ("Đủ".equals(comboBox.getValue())) {
                            row.setShortageQty("0");
                        }
                        getTableView().refresh();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ProcessItemRow row = getTableView().getItems().get(getIndex());
                    comboBox.setValue(row.getStatus());
                    setGraphic(comboBox);
                }
            }
        });

        // Custom Cell Factory for Shortage Quantity TextField
        shortageQtyCol.setCellFactory(col -> new TableCell<ProcessItemRow, String>() {
            private final TextField textField = new TextField();
            {
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        ProcessItemRow row = getTableView().getItems().get(getIndex());
                        // Allow digits only
                        if (!newVal.matches("\\d*")) {
                            textField.setText(newVal.replaceAll("[^\\d]", ""));
                        } else {
                            row.setShortageQty(newVal);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ProcessItemRow row = getTableView().getItems().get(getIndex());
                    textField.setText(row.getShortageQty());
                    textField.setDisable("Đủ".equals(row.getStatus()));
                    setGraphic(textField);
                }
            }
        });
    }

    private void loadOrderData() {
        try {
            Optional<PurchaseOrder> orderOpt = controller.getPurchaseOrder(orderId);
            if (orderOpt.isEmpty()) {
                new AlertMessage().errorMessage("Đơn hàng không tồn tại.");
                handleBack();
                return;
            }

            PurchaseOrder order = orderOpt.get();
            orderIdLabel.setText(String.valueOf(order.getOrderId()));
            
            // Site Name
            String siteCode = order.getSiteCode();
            String siteName = controller.resolveSiteName(siteCode);
            siteNameLabel.setText(siteCode + " - " + siteName);

            // Creator
            creatorNameLabel.setText(controller.resolveCreatorName(order.getCreatedBy()));

            // Date
            orderDateLabel.setText(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Delivery
            deliveryLabel.setText(order.getDeliveryMeans() == DeliveryMeans.SHIP_DELIVERY ? "Đường biển (Tàu)" : "Đường hàng không (Máy bay)");

            if (order.getStatus() != PurchaseOrderStatus.SENT && order.getStatus() != PurchaseOrderStatus.CONFIRMED) {
                new AlertMessage().errorMessage("Chỉ có thể xác nhận đơn hàng nhập kho ở trạng thái 'Chưa xác nhận'.");
                handleBack();
                return;
            }

            // Status SENT/CONFIRMED is shown to warehouse as "Chưa xác nhận".
            statusLabel.setText("Chưa xác nhận");

            // Load items
            List<PurchaseOrderItem> items = controller.getPurchaseOrderItems(orderId);
            itemsList.clear();
            for (PurchaseOrderItem item : items) {
                String merchName = controller.resolveMerchandiseName(item.getMerchandiseCode());
                itemsList.add(new ProcessItemRow(
                        item.getMerchandiseCode(),
                        merchName,
                        item.getQuantityOrdered(),
                        item.getUnit()
                ));
            }

        } catch (SQLException e) {
            new AlertMessage().errorMessage("Lỗi tải thông tin đơn hàng: " + e.getMessage());
            handleBack();
        }
    }

    private void initEventHandlers() {
        backBtn.setOnAction(event -> handleBack());
        approveBtn.setOnAction(event -> handleApprove());
    }

    private void handleApprove() {
        // Prepare list for controller validation
        List<ProcessPurchaseOrderController.ItemShortageResult> results = new java.util.ArrayList<>();
        for (ProcessItemRow row : itemsList) {
            String shortageText = row.getShortageQty().trim();
            int shortageVal = 0;
            if ("Thiếu".equals(row.getStatus())) {
                if (shortageText.isEmpty()) {
                    new AlertMessage().errorMessage("Vui lòng nhập số lượng thiếu cho mặt hàng: " + row.getMerchandiseName());
                    return;
                }
                try {
                    shortageVal = Integer.parseInt(shortageText);
                } catch (NumberFormatException e) {
                    new AlertMessage().errorMessage("Số lượng thiếu không hợp lệ cho mặt hàng: " + row.getMerchandiseName());
                    return;
                }
            }
            results.add(new ProcessPurchaseOrderController.ItemShortageResult(
                    row.getMerchandiseCode(),
                    row.getMerchandiseName(),
                    row.getStatus(),
                    row.getQuantityOrdered(),
                    shortageVal
            ));
        }

        boolean confirm = new AlertMessage().confirmationMessage("Bạn có chắc chắn muốn xác nhận duyệt đơn hàng nhập kho này?");
        if (!confirm) return;

        try {
            controller.validateAndApproveReceipt(orderId, results);

            // Build shortage details summary
            StringBuilder summary = new StringBuilder();
            summary.append("Duyệt đơn hàng nhập kho thành công!\n\n");
            summary.append("Kết quả kiểm tra hàng thực tế:\n");
            for (ProcessItemRow row : itemsList) {
                summary.append("- ").append(row.getMerchandiseCode())
                        .append(" (").append(row.getMerchandiseName()).append("): ");
                if ("Đủ".equals(row.getStatus())) {
                    summary.append("Đủ (Số lượng: ").append(row.getQuantityOrdered()).append(")\n");
                } else {
                    int shortageVal = Integer.parseInt(row.getShortageQty().trim());
                    int receivedVal = row.getQuantityOrdered() - shortageVal;
                    summary.append("Thiếu ").append(shortageVal)
                            .append(" / ").append(row.getQuantityOrdered())
                            .append(" (Thực nhận: ").append(receivedVal).append(")\n");
                }
            }

            new AlertMessage().successMessage(summary.toString());
            handleBack();
        } catch (IllegalArgumentException e) {
            new AlertMessage().errorMessage(e.getMessage());
        } catch (SQLException e) {
            new AlertMessage().errorMessage("Lỗi khi cập nhật trạng thái duyệt đơn hàng: " + e.getMessage());
        }
    }

    private void handleBack() {
        try {
            if (pageTitle.getScene() == null) {
                Platform.runLater(this::handleBack);
                return;
            }
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/purchase_order/filtered-purchase-order-view.fxml"));
            StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể quay lại danh sách đơn hàng: " + e.getMessage());
        }
    }

    public static class ProcessItemRow {
        private final String merchandiseCode;
        private final String merchandiseName;
        private final int quantityOrdered;
        private final String unit;
        private final SimpleStringProperty status = new SimpleStringProperty("Đủ");
        private final SimpleStringProperty shortageQty = new SimpleStringProperty("0");

        public ProcessItemRow(String merchandiseCode, String merchandiseName, int quantityOrdered, String unit) {
            this.merchandiseCode = merchandiseCode;
            this.merchandiseName = merchandiseName;
            this.quantityOrdered = quantityOrdered;
            this.unit = unit;
        }

        public String getMerchandiseCode() { return merchandiseCode; }
        public String getMerchandiseName() { return merchandiseName; }
        public int getQuantityOrdered() { return quantityOrdered; }
        public String getUnit() { return unit; }

        public String getStatus() { return status.get(); }
        public void setStatus(String val) { this.status.set(val); }
        public SimpleStringProperty statusProperty() { return status; }

        public String getShortageQty() { return shortageQty.get(); }
        public void setShortageQty(String val) { this.shortageQty.set(val); }
        public SimpleStringProperty shortageQtyProperty() { return shortageQty; }
    }
}
