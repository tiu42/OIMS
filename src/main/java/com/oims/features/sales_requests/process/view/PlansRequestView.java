package com.oims.features.sales_requests.process.view;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.User;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.features.sales_requests.process.controller.ProcessRequestController;
import com.oims.features.sales_requests.process.dto.*;
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
import java.util.*;

public class PlansRequestView implements Initializable {

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
    private TableView<PlanDTO> plansTable;

    @FXML
    private TableColumn<PlanDTO, String> planNameCol;

    @FXML
    private TableColumn<PlanDTO, String> planSitesCountCol;

    @FXML
    private TableColumn<PlanDTO, String> planPrefSitesCol;

    @FXML
    private TableColumn<PlanDTO, String> planPrefDelivCol;

    @FXML
    private TableColumn<PlanDTO, String> planTotalStockCol;

    @FXML
    private TableView<DisplayDetailRow> planDetailsTable;

    @FXML
    private TableColumn<DisplayDetailRow, String> detailSiteCol;

    @FXML
    private TableColumn<DisplayDetailRow, String> detailDeliveryCol;

    @FXML
    private TableColumn<DisplayDetailRow, String> detailMerchCol;

    @FXML
    private TableColumn<DisplayDetailRow, String> detailQtyCol;

    @FXML
    private Button confirmPlanBtn;

    @FXML
    private Button backToStep1Btn;

    private final ProcessRequestController controller = new ProcessRequestController();
    private final AlertMessage alertMessage = new AlertMessage();
    private final ObservableList<PlanDTO> plansList = FXCollections.observableArrayList();
    private final ObservableList<DisplayDetailRow> detailsList = FXCollections.observableArrayList();

    private Integer requestId;
    private Map<String, ItemConfig> configs = new HashMap<>();
    private Set<String> skippedItems = new HashSet<>();
    private boolean hasErrors = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestId = AppSession.getInstance().getSelectedRequestId();
        if (requestId == null) {
            alertMessage.errorMessage("Không tìm thấy mã yêu cầu cần xử lý.");
            navigateBackToDetail();
            return;
        }

        initTableColumns();
        loadRequestData();
        initEventHandlers();

        plansTable.setItems(plansList);
        planDetailsTable.setItems(detailsList);

        confirmPlanBtn.setDisable(true);
    }

    private void initTableColumns() {
        // Plans Table
        planNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPlanName()));
        planSitesCountCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSitesCountDisplay()));
        planPrefSitesCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrefSitesDisplay()));
        planPrefDelivCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrefDeliveryDisplay()));
        planTotalStockCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTotalStockDisplay()));

        // Plan Details Table
        detailSiteCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().siteName()));
        detailDeliveryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().deliveryMethod()));
        detailMerchCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseName()));
        detailQtyCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().quantity()));
    }

    private void loadRequestData() {
        try {
            Optional<SalesRequestDTO> requestOpt = controller.getSalesRequest(requestId);
            if (requestOpt.isEmpty()) {
                alertMessage.errorMessage("Yêu cầu nhập hàng không tồn tại.");
                navigateBackToDetail();
                return;
            }

            SalesRequestDTO request = requestOpt.get();
            requestIdLabel.setText(String.valueOf(request.requestId()));
            creatorNameLabel.setText(controller.getCreatorName(request.createdBy()));
            creationDateLabel.setText(request.createdDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            statusLabel.setText(request.statusText());
            statusLabel.getStyleClass().removeAll("status-pending", "status-processing", "status-completed", "status-error");
            switch (request.statusText()) {
                case "Chờ xử lý" -> statusLabel.getStyleClass().add("status-pending");
                case "Đang xử lý" -> statusLabel.getStyleClass().add("status-processing");
                case "Hoàn tất" -> statusLabel.getStyleClass().add("status-completed");
                case "Lỗi" -> statusLabel.getStyleClass().add("status-error");
            }

        } catch (SQLException e) {
            alertMessage.errorMessage("Lỗi tải thông tin yêu cầu: " + e.getMessage());
            navigateBackToDetail();
        }
    }

    private void initEventHandlers() {
        // Selection of plan in table
        plansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handlePlanSelection(newVal);
            } else {
                detailsList.clear();
                confirmPlanBtn.setDisable(true);
            }
        });

        // Action Buttons
        confirmPlanBtn.setOnAction(event -> handleConfirmPlan());
        backToStep1Btn.setOnAction(event -> handleBackToStep1());
    }

    public void setPlansData(List<PlanDTO> plans, Map<String, ItemConfig> configs, Set<String> skippedItems, boolean hasErrors) {
        this.configs = configs;
        this.skippedItems = skippedItems;
        this.hasErrors = hasErrors;
        this.plansList.setAll(plans);

        if (!plansList.isEmpty()) {
            plansTable.getSelectionModel().selectFirst();
        }
    }

    private void handlePlanSelection(PlanDTO plan) {
        detailsList.clear();
        for (AllocatedOrder order : plan.orders()) {
            for (AllocatedItem item : order.items()) {
                detailsList.add(new DisplayDetailRow(
                        order.siteCode() + " - " + order.siteName(),
                        order.deliveryMeans() == DeliveryMeans.SHIP_DELIVERY ? "Đường biển (Tàu)" : "Đường hàng không (Máy bay)",
                        item.merchandiseCode() + " - " + item.merchandiseName(),
                        item.quantity() + " " + item.unit()
                ));
            }
        }
        confirmPlanBtn.setDisable(false);
    }

    private void handleConfirmPlan() {
        PlanDTO selectedPlan = plansTable.getSelectionModel().getSelectedItem();
        if (selectedPlan == null) return;

        User currentUser = AppSession.getInstance().getCurrentUser();
        boolean confirm = new AlertMessage().confirmationMessage(
                "Bạn có chắc chắn muốn chọn " + selectedPlan.getPlanName() + " và tiến hành tạo đơn hàng tương ứng không?\n" +
                "Thao tác này sẽ cập nhật trạng thái yêu cầu."
        );

        if (!confirm) return;

        try {
            controller.savePlan(requestId, currentUser.getUserId(), selectedPlan, hasErrors);
            alertMessage.successMessage("Xử lý yêu cầu và lập đơn hàng thành công!");
            navigateBackToDetail();
        } catch (SQLException e) {
            alertMessage.errorMessage("Tạo đơn hàng thất bại: " + e.getMessage());
        }
    }

    private void handleBackToStep1() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oims/features/sales_requests/process-sales-request-view.fxml"));
            Parent view = loader.load();

            ProcessRequestView step1View = loader.getController();
            step1View.restoreState(configs, skippedItems, hasErrors);

            StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            alertMessage.errorMessage("Không thể quay lại màn hình cấu hình: " + e.getMessage());
        }
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

    // Helper record for table binding
    public record DisplayDetailRow(String siteName, String deliveryMethod, String merchandiseName, String quantity) {}
}
