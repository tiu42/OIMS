package com.oims.features.sales_requests.detail;

import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.features.sales_requests.edit.EditPermissionResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RequestDetailView implements Initializable {
    private static final double STATUS_POLL_INTERVAL_SECONDS = 3;

    public Label requestIdLabel;
    public Label creatorNameLabel;
    public Label creationDateLabel;
    public Label requestStatusLabel;
    public TableView<RequestItemTableRow> requestDetailTable;
    public TableColumn<RequestItemTableRow, String> merchIdColumn;
    public TableColumn<RequestItemTableRow, String> merchNameColumn;
    public TableColumn<RequestItemTableRow, String> quantityColumn;
    public TableColumn<RequestItemTableRow, String> unitColumn;
    public TableColumn<RequestItemTableRow, String> desiredDateColumn;
    public Button updateRequestBtn;
    public Button deleteRequestBtn;
    public Button processRequestBtn;
    public Button viewProcessResultBtn;

    private final RequestDetailController controller = new RequestDetailController();
    private final AlertMessage alertMessage = new AlertMessage();
    private final ObservableList<RequestItemTableRow> salesRequestItemList = FXCollections.observableArrayList();
    private Timeline statusCheckTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        requestDetailTable.setItems(salesRequestItemList);

        if (!refreshFromDatabase(true)) {
            return;
        }

        configureButtonPermissions();

        updateRequestBtn.setOnAction(event -> handleUpdateRequest());
        processRequestBtn.setOnAction(event -> handleProcessRequest());
        viewProcessResultBtn.setOnAction(event -> handleViewProcessResult());

        startStatusPolling();
    }

    private void startStatusPolling() {
        statusCheckTimeline = new Timeline(new KeyFrame(
                Duration.seconds(STATUS_POLL_INTERVAL_SECONDS),
                event -> refreshFromDatabase(false)
        ));
        statusCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        statusCheckTimeline.play();
    }

    private void stopStatusPolling() {
        if (statusCheckTimeline != null) {
            statusCheckTimeline.stop();
        }
    }

    private boolean refreshFromDatabase(boolean showLoadError) {
        try {
            RequestDetailController.RequestDetailDTO requestData = controller.loadRequestData();
            applyRequestMetadata(requestData);
            salesRequestItemList.setAll(controller.loadTableData());
            configureButtonPermissions();
            return true;
        } catch (SQLException exception) {
            if (showLoadError) {
                alertMessage.errorMessage("Không thể tải chi tiết yêu cầu: " + exception.getMessage());
            }
            return false;
        }
    }

    private void configureButtonPermissions() {
        User currentUser = AppSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateRequestBtn.setVisible(false);
            updateRequestBtn.setManaged(false);
            deleteRequestBtn.setVisible(false);
            deleteRequestBtn.setManaged(false);
            processRequestBtn.setVisible(false);
            processRequestBtn.setManaged(false);
            viewProcessResultBtn.setVisible(false);
            viewProcessResultBtn.setManaged(false);
            return;
        }

        UserRole role = currentUser.getRole();

        boolean isSales = (role == UserRole.SALES);
        updateRequestBtn.setVisible(isSales);
        updateRequestBtn.setManaged(isSales);
        deleteRequestBtn.setVisible(isSales);
        deleteRequestBtn.setManaged(isSales);

        boolean isOverseas = (role == UserRole.OVERSEAS_ORDER);
        processRequestBtn.setVisible(isOverseas);
        processRequestBtn.setManaged(isOverseas);

        boolean isSalesOrOverseas = (role == UserRole.SALES || role == UserRole.OVERSEAS_ORDER);
        viewProcessResultBtn.setVisible(isSalesOrOverseas);
        viewProcessResultBtn.setManaged(isSalesOrOverseas);

        SalesRequestStatus status = controller.getSalesRequestStatus();
        boolean isProcessed = (status == SalesRequestStatus.COMPLETED || status == SalesRequestStatus.ERROR);
        viewProcessResultBtn.setDisable(!isProcessed);

        boolean canEditOrCancel = controller.canEditRequest();
        updateRequestBtn.setDisable(!canEditOrCancel);
        deleteRequestBtn.setDisable(!canEditOrCancel);

        boolean canProcess = controller.canBeginProcessing();
        processRequestBtn.setDisable(!canProcess);
    }

    private void applyRequestMetadata(RequestDetailController.RequestDetailDTO requestData) {
        requestIdLabel.setText(requestData.id());
        creatorNameLabel.setText(requestData.creatorName());
        creationDateLabel.setText(requestData.creationDate());

        String statusText = requestData.status();
        requestStatusLabel.setText(statusText);
        requestStatusLabel.getStyleClass().removeAll("status-pending", "status-processing", "status-completed", "status-error");
        if ("Chờ xử lý".equals(statusText)) {
            requestStatusLabel.getStyleClass().add("status-pending");
        } else if ("Đang xử lý".equals(statusText)) {
            requestStatusLabel.getStyleClass().add("status-processing");
        } else if ("Hoàn tất".equals(statusText)) {
            requestStatusLabel.getStyleClass().add("status-completed");
        } else if ("Lỗi".equals(statusText)) {
            requestStatusLabel.getStyleClass().add("status-error");
        }
    }

    private void configureColumns() {
        merchIdColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getItemId()));
        merchNameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().itemName()));
        quantityColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getQuantity().toString()));
        unitColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUnit()));
        desiredDateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDesiredDateDisplay()));
    }

    private void handleUpdateRequest() {
        stopStatusPolling();
        try {
            EditPermissionResult permission = controller.checkEditPermission();
            if (!permission.canEdit()) {
                alertMessage.errorMessage(permission.blockedMessage());
                refreshFromDatabase(false);
                startStatusPolling();
                return;
            }

            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/edit-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) requestIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (SQLException exception) {
            alertMessage.errorMessage("Không thể kiểm tra trạng thái yêu cầu: " + exception.getMessage());
            startStatusPolling();
        } catch (IOException exception) {
            alertMessage.errorMessage("Không thể mở màn hình chỉnh sửa: " + exception.getMessage());
            startStatusPolling();
        }
    }

    private void handleProcessRequest() {
        stopStatusPolling();
        try {
            if (!refreshFromDatabase(true)) {
                startStatusPolling();
                return;
            }

            if (!controller.canBeginProcessing()) {
                SalesRequestStatus status = controller.getSalesRequestStatus();
                String message = switch (status) {
                    case COMPLETED -> "Yêu cầu đã được xử lý xong. Không thể xử lý lại.";
                    case ERROR -> "Yêu cầu đã được xử lý (có lỗi). Không thể xử lý lại.";
                    default -> "Yêu cầu không còn ở trạng thái có thể xử lý.";
                };
                alertMessage.errorMessage(message);
                configureButtonPermissions();
                startStatusPolling();
                return;
            }

            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/process-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) requestIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException exception) {
            alertMessage.errorMessage("Không thể mở màn hình xử lý yêu cầu: " + exception.getMessage());
            startStatusPolling();
        }
    }

    private void handleViewProcessResult() {
        stopStatusPolling();
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/process-result-view.fxml"));
            StackPane contentArea = (StackPane) requestIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException exception) {
            alertMessage.errorMessage("Không thể mở màn hình kết quả xử lý: " + exception.getMessage());
        } finally {
            startStatusPolling();
        }
    }
}
