package com.oims.features.sales_requests.process;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.PurchaseOrder;
import com.oims.core.model.PurchaseOrderItem;
import com.oims.core.model.SalesRequest;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
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

public class ProcessResultView implements Initializable {

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
    private TableView<PurchaseOrder> ordersTable;

    @FXML
    private TableColumn<PurchaseOrder, String> orderIdCol;

    @FXML
    private TableColumn<PurchaseOrder, String> orderSiteCol;

    @FXML
    private TableColumn<PurchaseOrder, String> orderDeliveryCol;

    @FXML
    private TableColumn<PurchaseOrder, String> orderStatusCol;

    @FXML
    private TableView<DisplayOrderItemRow> orderItemsTable;

    @FXML
    private TableColumn<DisplayOrderItemRow, String> itemMerchCol;

    @FXML
    private TableColumn<DisplayOrderItemRow, String> itemQtyCol;

    @FXML
    private TableColumn<DisplayOrderItemRow, String> itemUnitCol;

    @FXML
    private TableView<ProcessedErrorDTO> errorsTable;

    @FXML
    private TableColumn<ProcessedErrorDTO, String> errorMerchCol;

    @FXML
    private TableColumn<ProcessedErrorDTO, String> errorQtyReqCol;

    @FXML
    private TableColumn<ProcessedErrorDTO, String> errorQtyAllocCol;

    @FXML
    private TableColumn<ProcessedErrorDTO, String> errorQtyMissCol;

    @FXML
    private TableColumn<ProcessedErrorDTO, String> errorReasonCol;

    @FXML
    private Button backBtn;

    private final ProcessResultController controller = new ProcessResultController();
    private final ObservableList<PurchaseOrder> ordersList = FXCollections.observableArrayList();
    private final ObservableList<DisplayOrderItemRow> orderItemsList = FXCollections.observableArrayList();
    private final ObservableList<ProcessedErrorDTO> errorsList = FXCollections.observableArrayList();

    private Integer requestId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestId = AppSession.getInstance().getSelectedRequestId();
        if (requestId == null) {
            new AlertMessage().errorMessage("Không tìm thấy mã yêu cầu cần xem kết quả.");
            handleBack();
            return;
        }

        initTableColumns();
        loadRequestData();
        loadResultData();
        initEventHandlers();

        ordersTable.setItems(ordersList);
        orderItemsTable.setItems(orderItemsList);
        errorsTable.setItems(errorsList);
    }

    private void initTableColumns() {
        // Orders Table
        orderIdCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getOrderId())));
        orderSiteCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSiteCode() + " - " + controller.resolveSiteName(cell.getValue().getSiteCode())));
        orderDeliveryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDeliveryMeans() == DeliveryMeans.SHIP_DELIVERY ? "Đường biển (Tàu)" : "Đường hàng không (Máy bay)"));
        orderStatusCol.setCellValueFactory(cell -> new SimpleStringProperty(switch (cell.getValue().getStatus()) {
            case DRAFT -> "Bản nháp";
            case SENT -> "Đã gửi";
            case CONFIRMED -> "Xác nhận";
            case DELIVERED -> "Đã giao";
            case CANCELLED -> "Đã hủy";
        }));

        // Order Items Table
        itemMerchCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseInfo()));
        itemQtyCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().quantity()));
        itemUnitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().unit()));

        // Errors Table
        errorMerchCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseCode() + " - " + cell.getValue().merchandiseName()));
        errorQtyReqCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().requestedQty())));
        errorQtyAllocCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().allocatedQty())));
        errorQtyMissCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().missingQty() + " " + cell.getValue().unit()));
        errorReasonCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().reason()));
    }

    private void loadRequestData() {
        try {
            Optional<SalesRequest> requestOpt = controller.getSalesRequest(requestId);
            if (requestOpt.isEmpty()) {
                new AlertMessage().errorMessage("Yêu cầu nhập hàng không tồn tại.");
                handleBack();
                return;
            }

            SalesRequest request = requestOpt.get();
            requestIdLabel.setText(String.valueOf(request.getRequestId()));
            creatorNameLabel.setText(controller.getCreatorName(request.getCreatedBy()));
            creationDateLabel.setText(request.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            String statusText = switch (request.getStatus()) {
                case PENDING -> "Chờ xử lý";
                case PROCESSING -> "Đang xử lý";
                case COMPLETED -> "Hoàn tất";
                case ERROR -> "Lỗi";
            };
            statusLabel.setText(statusText);
            statusLabel.getStyleClass().removeAll("status-pending", "status-processing", "status-completed", "status-error");
            switch (request.getStatus()) {
                case PENDING -> statusLabel.getStyleClass().add("status-pending");
                case PROCESSING -> statusLabel.getStyleClass().add("status-processing");
                case COMPLETED -> statusLabel.getStyleClass().add("status-completed");
                case ERROR -> statusLabel.getStyleClass().add("status-error");
            }
        } catch (SQLException e) {
            new AlertMessage().errorMessage("Lỗi tải thông tin yêu cầu: " + e.getMessage());
            handleBack();
        }
    }

    private void loadResultData() {
        try {
            // Load created purchase orders
            List<PurchaseOrder> pos = controller.getPurchaseOrders(requestId);
            ordersList.setAll(pos);

            // Select first purchase order if any
            if (!ordersList.isEmpty()) {
                ordersTable.getSelectionModel().selectFirst();
            }

            // Load and compute errors
            List<ProcessedErrorDTO> errors = controller.getProcessingErrors(requestId);
            errorsList.setAll(errors);
        } catch (SQLException e) {
            new AlertMessage().errorMessage("Lỗi tải kết quả xử lý: " + e.getMessage());
        }
    }

    private void initEventHandlers() {
        // Selection of purchase order in table
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleOrderSelection(newVal);
            } else {
                orderItemsList.clear();
            }
        });

        // Back button
        backBtn.setOnAction(event -> handleBack());
    }

    private void handleOrderSelection(PurchaseOrder order) {
        try {
            orderItemsList.clear();
            List<PurchaseOrderItem> items = controller.getPurchaseOrderItems(order.getOrderId());
            for (PurchaseOrderItem item : items) {
                String merchName = controller.resolveMerchandiseName(item.getMerchandiseCode());
                orderItemsList.add(new DisplayOrderItemRow(
                        item.getMerchandiseCode() + " - " + merchName,
                        String.valueOf(item.getQuantityOrdered()),
                        item.getUnit()
                ));
            }
        } catch (SQLException e) {
            new AlertMessage().errorMessage("Lỗi tải chi tiết đơn hàng: " + e.getMessage());
        }
    }

    private void handleBack() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/detail-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể quay lại chi tiết yêu cầu: " + e.getMessage());
        }
    }

    // Helper record for table binding
    public record DisplayOrderItemRow(String merchandiseInfo, String quantity, String unit) {}
}
