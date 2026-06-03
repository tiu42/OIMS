package com.oims.features.purchase_order.detail;

import com.oims.core.dao.*;
import com.oims.core.model.*;
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

public class OderDetailView implements Initializable {

    @FXML
    private Label pageTitle;

    @FXML
    private Label orderIdLabel;

    @FXML
    private Label requestIdLabel;

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
    private TableView<DisplayItemRow> orderItemsTable;

    @FXML
    private TableColumn<DisplayItemRow, String> merchCodeCol;

    @FXML
    private TableColumn<DisplayItemRow, String> merchNameCol;

    @FXML
    private TableColumn<DisplayItemRow, String> quantityCol;

    @FXML
    private TableColumn<DisplayItemRow, String> unitCol;

    @FXML
    private Button backBtn;

    @FXML
    private Button sendOrderBtn;

    @FXML
    private Button processCanceledBtn;

    private final PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
    private final PurchaseOrderItemDao purchaseOrderItemDao = new PurchaseOrderItemDao();
    private final UserDao userDao = new UserDao();
    private final ImportSiteDao importSiteDao = new ImportSiteDao();
    private final MerchandiseDao merchandiseDao = new MerchandiseDao();

    private final ObservableList<DisplayItemRow> itemsList = FXCollections.observableArrayList();
    private Integer orderId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderId = AppSession.getInstance().getSelectedOrderId();
        if (orderId == null) {
            new AlertMessage().errorMessage("Không tìm thấy mã đơn hàng cần xem chi tiết.");
            handleBack();
            return;
        }

        initTableColumns();
        loadOrderData();
        initEventHandlers();

        orderItemsTable.setItems(itemsList);
    }

    private void initTableColumns() {
        merchCodeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseCode()));
        merchNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseName()));
        quantityCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().quantity()));
        unitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().unit()));
    }

    private void loadOrderData() {
        try {
            Optional<PurchaseOrder> orderOpt = purchaseOrderDao.findById(orderId);
            if (orderOpt.isEmpty()) {
                new AlertMessage().errorMessage("Đơn hàng không tồn tại.");
                handleBack();
                return;
            }

            PurchaseOrder order = orderOpt.get();
            orderIdLabel.setText(String.valueOf(order.getOrderId()));
            requestIdLabel.setText(String.valueOf(order.getRequestId()));
            
            // Site Name
            String siteCode = order.getSiteCode();
            String siteName = resolveSiteName(siteCode);
            siteNameLabel.setText(siteCode + " - " + siteName);

            // Creator
            creatorNameLabel.setText(resolveCreatorName(order.getCreatedBy()));

            // Date
            orderDateLabel.setText(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Delivery
            deliveryLabel.setText(order.getDeliveryMeans() == DeliveryMeans.SHIP_DELIVERY ? "Đường biển (Tàu)" : "Đường hàng không (Máy bay)");

            // Status
            User currentUser = AppSession.getInstance().getCurrentUser();
            String statusText = switch (order.getStatus()) {
                case DRAFT -> "Bản nháp";
                case SENT -> "Đã gửi";
                case CONFIRMED -> (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) ? "Chưa xác nhận" : "Xác nhận";
                case DELIVERED -> (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) ? "Đã xác nhận" : "Đã giao";
                case CANCELLED -> "Đã hủy";
            };
            statusLabel.setText(statusText);
            statusLabel.getStyleClass().removeAll("status-draft", "status-sent", "status-confirmed", "status-delivered", "status-cancelled");
            switch (order.getStatus()) {
                case DRAFT -> statusLabel.getStyleClass().add("status-draft");
                case SENT -> statusLabel.getStyleClass().add("status-sent");
                case CONFIRMED -> statusLabel.getStyleClass().add("status-confirmed");
                case DELIVERED -> statusLabel.getStyleClass().add("status-delivered");
                case CANCELLED -> statusLabel.getStyleClass().add("status-cancelled");
            }

            // Set button visibility based on status
            if (order.getStatus() == PurchaseOrderStatus.DRAFT) {
                sendOrderBtn.setVisible(true);
                sendOrderBtn.setManaged(true);
                processCanceledBtn.setVisible(false);
                processCanceledBtn.setManaged(false);
            } else if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
                sendOrderBtn.setVisible(false);
                sendOrderBtn.setManaged(false);
                processCanceledBtn.setVisible(true);
                processCanceledBtn.setManaged(true);
            } else {
                sendOrderBtn.setVisible(false);
                sendOrderBtn.setManaged(false);
                processCanceledBtn.setVisible(false);
                processCanceledBtn.setManaged(false);
            }

            // Load items
            List<PurchaseOrderItem> items = purchaseOrderItemDao.findByOrderId(orderId);
            itemsList.clear();
            for (PurchaseOrderItem item : items) {
                String merchName = resolveMerchandiseName(item.getMerchandiseCode());
                itemsList.add(new DisplayItemRow(
                        item.getMerchandiseCode(),
                        merchName,
                        String.valueOf(item.getQuantityOrdered()),
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
        sendOrderBtn.setOnAction(event -> handleSendOrder());
        processCanceledBtn.setOnAction(event -> handleProcessCanceled());
    }

    private void handleSendOrder() {
        boolean confirm = new AlertMessage().confirmationMessage("Bạn có chắc chắn muốn gửi đơn hàng này không?");
        if (!confirm) return;

        try {
            Optional<PurchaseOrder> orderOpt = purchaseOrderDao.findById(orderId);
            if (orderOpt.isPresent()) {
                PurchaseOrder order = orderOpt.get();
                order.setStatus(PurchaseOrderStatus.SENT);
                purchaseOrderDao.update(order);
                new AlertMessage().successMessage("Gửi đơn hàng thành công!");
                loadOrderData();
            }
        } catch (SQLException e) {
            new AlertMessage().errorMessage("Gửi đơn hàng thất bại: " + e.getMessage());
        }
    }

    private void handleProcessCanceled() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/purchase_order/process-canceled-purchase-order-view.fxml"));
            StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể mở màn hình xử lý đơn hàng bị hủy: " + e.getMessage());
        }
    }

    private void handleBack() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/purchase_order/filtered-purchase-order-view.fxml"));
            StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể quay lại danh sách đơn hàng: " + e.getMessage());
        }
    }

    private String resolveSiteName(String siteCode) {
        try {
            return importSiteDao.findById(siteCode)
                    .map(ImportSite::getSiteName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    private String resolveCreatorName(Integer userId) {
        if (userId == null) return "Không xác định";
        try {
            return userDao.findById(userId)
                    .map(user -> {
                        String fullName = user.getFullName();
                        if (fullName != null && !fullName.isBlank()) return fullName;
                        return user.getUsername() == null || user.getUsername().isBlank() ? "Không xác định" : user.getUsername();
                    })
                    .orElse("Người dùng #" + userId);
        } catch (SQLException e) {
            return "Người dùng #" + userId;
        }
    }

    private String resolveMerchandiseName(String merchandiseCode) {
        try {
            return merchandiseDao.findById(merchandiseCode)
                    .map(Merchandise::getMerchandiseName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    public record DisplayItemRow(String merchandiseCode, String merchandiseName, String quantity, String unit) {}
}
