package com.oims.features.sales_requests.detail;

import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.features.sales_requests.list.RequestListMode;
import com.oims.features.sales_requests.list.RequestListRow;
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

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class RequestDetailView implements Initializable {
    public Label requestIdLabel;
    public Label creatorNameLabel;
    public Label creationDateLabel;
    public Label requestStatusLabel;
    public TableView<RequestItemTableRow> requestDetailTable;
    public TableColumn<RequestItemTableRow,String> merchIdColumn;
    public TableColumn<RequestItemTableRow,String> merchNameColumn;
    public TableColumn<RequestItemTableRow,String> quantityColumn;
    public TableColumn<RequestItemTableRow,String> unitColumn;
    public TableColumn<RequestItemTableRow, String> desiredDateColumn;
    public Button updateRequestBtn;
    public Button deleteRequestBtn;
    public Button processRequestBtn;
    public Button viewProcessResultBtn;

    private final RequestDetailController controller = new RequestDetailController();
    private ObservableList<RequestItemTableRow> salesRequestItemList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        currentUser = AppSession.getInstance().getCurrentUser();
//        mode = RequestListMode.fromCurrentUser(currentUser);

        configureStaticContent();
        configureColumns();

        loadData();
        requestDetailTable.setItems(salesRequestItemList);
        
        configureButtonPermissions();

        updateRequestBtn.setOnAction(event -> handleUpdateRequest());
        processRequestBtn.setOnAction(event -> handleProcessRequest());
        viewProcessResultBtn.setOnAction(event -> handleViewProcessResult());
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

        // 1. Chỉ người dùng SALES mới thấy nút chỉnh sửa yêu cầu & hủy yêu cầu
        boolean isSales = (role == UserRole.SALES);
        updateRequestBtn.setVisible(isSales);
        updateRequestBtn.setManaged(isSales);
        deleteRequestBtn.setVisible(isSales);
        deleteRequestBtn.setManaged(isSales);

        // 2. Chỉ OVERSEAS mới thấy nút Xử lý yêu cầu
        boolean isOverseas = (role == UserRole.OVERSEAS_ORDER);
        processRequestBtn.setVisible(isOverseas);
        processRequestBtn.setManaged(isOverseas);

        // 3. Cả 2 đều có thể thấy nút Xem kết quả xử lý
        boolean isSalesOrOverseas = (role == UserRole.SALES || role == UserRole.OVERSEAS_ORDER);
        viewProcessResultBtn.setVisible(isSalesOrOverseas);
        viewProcessResultBtn.setManaged(isSalesOrOverseas);

        // Nút Xem kết quả xử lý bị disable nếu yêu cầu chưa được xử lý xong (status là PENDING hoặc PROCESSING)
        SalesRequestStatus status = controller.getSalesRequestStatus();
        boolean isProcessed = (status == SalesRequestStatus.COMPLETED || status == SalesRequestStatus.ERROR);
        viewProcessResultBtn.setDisable(!isProcessed);

        // Disable nút chỉnh sửa yêu cầu & hủy yêu cầu nếu yêu cầu đó đã/đang được xử lý (trạng thái khác PENDING)
        boolean canEditOrCancel = (status == SalesRequestStatus.PENDING);
        updateRequestBtn.setDisable(!canEditOrCancel);
        deleteRequestBtn.setDisable(!canEditOrCancel);

        // Disable nút xử lý yêu cầu nếu yêu cầu đó đã được xử lý (trạng thái khác PENDING và PROCESSING)
        boolean canProcess = (status == SalesRequestStatus.PENDING || status == SalesRequestStatus.PROCESSING);
        processRequestBtn.setDisable(!canProcess);
    }

    private void configureStaticContent(){
        try {
            RequestDetailController.RequestDetailDTO requestData = controller.loadRequestData();
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureColumns(){
        merchIdColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getItemId()));
        merchNameColumn.setCellValueFactory(cell-> new ReadOnlyStringWrapper(cell.getValue().itemName()));
        quantityColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getQuantity().toString()));
        unitColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUnit()));
        desiredDateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDesiredDateDisplay()));
    }

    private void loadData(){
        try{
            List<RequestItemTableRow> rows = controller.loadTableData();
            salesRequestItemList.setAll(rows);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleUpdateRequest() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/edit-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) requestIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể mở màn hình chỉnh sửa: " + e.getMessage());
        }
    }

    private void handleProcessRequest() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/process-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) requestIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể mở màn hình xử lý yêu cầu: " + e.getMessage());
        }
    }

    private void handleViewProcessResult() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/process-result-view.fxml"));
            StackPane contentArea = (StackPane) requestIdLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            new AlertMessage().errorMessage("Không thể mở màn hình kết quả xử lý: " + e.getMessage());
        }
    }
}
