package com.oims.features.sales_requests.list;

import com.oims.core.dao.SalesRequestDao;
import com.oims.core.dao.UserDao;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import javafx.beans.property.ReadOnlyStringWrapper;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RequestListController implements Initializable {

    private static final int PAGE_SIZE = 10;
    private static final int MAX_VISIBLE_PAGE_BUTTONS = 5;

    private final SalesRequestDao salesRequestDao = new SalesRequestDao();
    private final UserDao userDao = new UserDao();
    private final AlertMessage alertMessage = new AlertMessage();

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<RequestListRow> importRequestTable;

    @FXML
    private TableColumn<RequestListRow, Void> selectColumn;

    @FXML
    private TableColumn<RequestListRow, String> requestIdColumn;

    @FXML
    private TableColumn<RequestListRow, String> creatorColumn;

    @FXML
    private TableColumn<RequestListRow, String> createdDateColumn;

    @FXML
    private TableColumn<RequestListRow, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label totalRequestsLabel;

    @FXML
    private Label pendingRequestsLabel;

    @FXML
    private Label errorRequestsLabel;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button pagePrevButton;

    @FXML
    private Button pageButton1;

    @FXML
    private Button pageButton2;

    @FXML
    private Button pageButton3;

    @FXML
    private Button pageButton4;

    @FXML
    private Button pageButton5;

    @FXML
    private Button pageNextButton;

    private final ObservableList<RequestListRow> tableItems = FXCollections.observableArrayList();
    private final List<Button> pageButtons = new ArrayList<>();
    private List<RequestListRow> allRows = List.of();
    private int currentPage = 1;
    private boolean suppressFilterEvents;
    private RequestListMode mode;
    private User currentUser;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AppSession.getInstance().getCurrentUser();
        mode = RequestListMode.fromCurrentUser(currentUser);

        pageButtons.addAll(Arrays.asList(pageButton1, pageButton2, pageButton3, pageButton4, pageButton5));

        configureStaticContent();
        configureColumns();
        configureRowDoubleClick();
        configurePaginationControls();
        configureFilters();

        importRequestTable.setItems(tableItems);
        loadData();
    }

    @FXML
    private void handleRefresh() {
        suppressFilterEvents = true;
        try {
            searchField.clear();
            statusFilterComboBox.getSelectionModel().selectFirst();
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);

            importRequestTable.getSelectionModel().clearSelection();
        } finally {
            suppressFilterEvents = false;
        }

        loadData();
    }

    private void configureStaticContent() {
        titleLabel.setText(mode.getTitle());
        searchField.setPromptText(mode.getSearchPrompt());
    }

    private void configureColumns() {
        selectColumn.setCellFactory(col -> new TableCell<RequestListRow, Void>() {
            private final RadioButton radio = new RadioButton();
            {
                radio.setOnMouseClicked(event -> {
                    if (getIndex() >= getTableView().getItems().size()) return;

                    RequestListRow row = getTableView().getItems().get(getIndex());
                    AppSession session = AppSession.getInstance();

                    // So sánh với ID trong AppSession
                    if (session.getSelectedRequestId() != null && session.getSelectedRequestId().equals(row.getRequestId())) {
                        session.clearSelectedRequest(); // Hủy chọn toàn cục
                        getTableView().getSelectionModel().clearSelection();
                    } else {
                        session.setSelectedRequestId(row.getRequestId()); // Lưu vào toàn cục
                        getTableView().getSelectionModel().select(getIndex());
                    }

                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(radio);
                    RequestListRow row = getTableView().getItems().get(getIndex());

                    // Đọc trạng thái từ AppSession để hiển thị lên UI
                    Integer globalSelectedId = AppSession.getInstance().getSelectedRequestId();
                    boolean isSelected = (globalSelectedId != null && globalSelectedId.equals(row.getRequestId()));

                    radio.setSelected(isSelected);
                    if (isSelected) {
                        getTableView().getSelectionModel().select(getIndex());
                    }
                }
            }
        });
        requestIdColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRequestIdDisplay()));
        creatorColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().creatorName()));
        createdDateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCreatedDateDisplay()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatusLabel()));
    }

    private void configureRowDoubleClick() {
        importRequestTable.setRowFactory(table -> {
            TableRow<RequestListRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty() || event.getClickCount() != 2) {
                    return;
                }
                openRequestDetail(row.getItem());
            });
            return row;
        });
    }

    private void openRequestDetail(RequestListRow row) {
        if (row == null || row.getRequestId() == null) {
            return;
        }

        AppSession.getInstance().setSelectedRequestId(row.getRequestId());
        importRequestTable.getSelectionModel().select(row);
        importRequestTable.refresh();

        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/sales_requests/detail-sales-request-view.fxml"));
            StackPane contentArea = (StackPane) importRequestTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            alertMessage.errorMessage("Không thể mở màn hình chi tiết yêu cầu: " + e.getMessage());
        }
    }

    private void configurePaginationControls() {
        refreshButton.setOnAction(event -> handleRefresh());
        pagePrevButton.setOnAction(event -> goToPage(currentPage - 1));
        pageNextButton.setOnAction(event -> goToPage(currentPage + 1));
    }

    private void configureFilters() {
        suppressFilterEvents = true;
        try {
            statusFilterComboBox.setItems(FXCollections.observableArrayList(
                    "Tất cả",
                    "Chờ xử lý",
                    "Đang xử lý",
                    "Hoàn tất",
                    "Lỗi"
            ));
            statusFilterComboBox.getSelectionModel().selectFirst();
        } finally {
            suppressFilterEvents = false;
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
    }

    private void loadData() {
        try {
            List<SalesRequest> requests = loadRequestsForMode();
            Map<Integer, String> creatorCache = new HashMap<>();
            List<RequestListRow> rows = new ArrayList<>();

            for (SalesRequest request : requests) {
                rows.add(new RequestListRow(request, resolveCreatorName(request.getCreatedBy(), creatorCache)));
            }

            allRows = rows;
            updateSummaryLabels(allRows);
            currentPage = 1;
            renderPage();
        } catch (SQLException exception) {
            allRows = List.of();
            tableItems.clear();
            updateSummaryLabels(allRows);
            updatePaginationState(0, 0);
            alertMessage.errorMessage("Không thể tải danh sách yêu cầu: " + exception.getMessage());
        }
    }

    private List<SalesRequest> loadRequestsForMode() throws SQLException {
        if (mode == RequestListMode.SALES) {
            if (currentUser == null || currentUser.getUserId() == null) {
                return List.of();
            }

            return salesRequestDao.findByCreatedBy(currentUser.getUserId());
        }

        return salesRequestDao.findAll();
    }

    private void applyFiltersAndRender() {
        if (suppressFilterEvents) {
            return;
        }

        currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        List<RequestListRow> filteredRows = filterRows();
        int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = Math.max(0, (currentPage - 1) * PAGE_SIZE);
        int toIndex = Math.min(filteredRows.size(), fromIndex + PAGE_SIZE);

        tableItems.setAll(filteredRows.subList(fromIndex, toIndex));
        updatePaginationState(totalPages, filteredRows.size());
    }

    private List<RequestListRow> filterRows() {
        String keyword = normalize(searchField.getText());
        String selectedStatus = statusFilterComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            LocalDate swap = fromDate;
            fromDate = toDate;
            toDate = swap;
        }

        final LocalDate startDate = fromDate;
        final LocalDate endDate = toDate;

        return allRows.stream()
                .filter(row -> matchesKeyword(row, keyword))
                .filter(row -> matchesStatus(row, selectedStatus))
                .filter(row -> matchesDateRange(row, startDate, endDate))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(RequestListRow row, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        return row.getSearchText().contains(keyword);
    }

    private boolean matchesStatus(RequestListRow row, String selectedStatus) {
        if (selectedStatus == null || selectedStatus.isBlank() || "Tất cả".equals(selectedStatus)) {
            return true;
        }

        return selectedStatus.equalsIgnoreCase(row.getStatusLabel());
    }

    private boolean matchesDateRange(RequestListRow row, LocalDate startDate, LocalDate endDate) {
        LocalDate rowDate = row.getCreatedDate();
        if (rowDate == null) {
            return false;
        }

        if (startDate != null && rowDate.isBefore(startDate)) {
            return false;
        }

        return endDate == null || !rowDate.isAfter(endDate);
    }

    private void updateSummaryLabels(List<RequestListRow> rows) {
        totalRequestsLabel.setText(String.valueOf(rows.size()));
        pendingRequestsLabel.setText(String.valueOf(rows.stream().filter(row -> row.getStatus() == SalesRequestStatus.PENDING).count()));
        errorRequestsLabel.setText(String.valueOf(rows.stream().filter(row -> row.getStatus() == SalesRequestStatus.ERROR).count()));
    }

    private void updatePaginationState(int totalPages, int itemCount) {
        int safeTotalPages = Math.max(1, totalPages);
        pageInfoLabel.setText("Trang " + currentPage + " / " + safeTotalPages);
        pagePrevButton.setDisable(currentPage <= 1);
        pageNextButton.setDisable(currentPage >= safeTotalPages);

        int windowStart = Math.max(1, currentPage - (MAX_VISIBLE_PAGE_BUTTONS / 2));
        int windowEnd = Math.min(safeTotalPages, windowStart + MAX_VISIBLE_PAGE_BUTTONS - 1);
        if (windowEnd - windowStart + 1 < MAX_VISIBLE_PAGE_BUTTONS) {
            windowStart = Math.max(1, windowEnd - MAX_VISIBLE_PAGE_BUTTONS + 1);
        }

        for (int index = 0; index < pageButtons.size(); index++) {
            Button button = pageButtons.get(index);
            int pageNumber = windowStart + index;
            boolean visible = pageNumber <= safeTotalPages;
            button.setVisible(visible);
            button.setManaged(visible);
            if (!visible) {
                continue;
            }

            button.setText(String.valueOf(pageNumber));
            int targetPage = pageNumber;
            button.setOnAction(event -> goToPage(targetPage));
        }

        if (itemCount == 0) {
            pageInfoLabel.setText("Trang 1 / 1");
        }
    }

    private void goToPage(int targetPage) {
        List<RequestListRow> filteredRows = filterRows();
        int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) PAGE_SIZE));
        currentPage = Math.max(1, Math.min(targetPage, totalPages));

        int fromIndex = Math.max(0, (currentPage - 1) * PAGE_SIZE);
        int toIndex = Math.min(filteredRows.size(), fromIndex + PAGE_SIZE);
        tableItems.setAll(filteredRows.subList(fromIndex, toIndex));
        updatePaginationState(totalPages, filteredRows.size());
    }

    private String resolveCreatorName(Integer userId, Map<Integer, String> cache) {
        if (userId == null) {
            return "Không xác định";
        }

        if (cache.containsKey(userId)) {
            return cache.get(userId);
        }

        try {
            String creatorName = userDao.findById(userId)
                    .map(this::formatUserName)
                    .orElse("Người dùng #" + userId);
            cache.put(userId, creatorName);
            return creatorName;
        } catch (SQLException exception) {
            String fallback = "Người dùng #" + userId;
            cache.put(userId, fallback);
            return fallback;
        }
    }

    private String formatUserName(User user) {
        if (user == null) {
            return "Không xác định";
        }

        String fullName = user.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }

        return user.getUsername() == null || user.getUsername().isBlank() ? "Không xác định" : user.getUsername();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
