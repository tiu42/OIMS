package com.oims.features.purchase_order.list;

import com.oims.core.dao.PurchaseOrderDao;
import com.oims.core.dao.ImportSiteDao;
import com.oims.core.dao.UserDao;
import com.oims.core.model.*;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class OrderListView implements Initializable {

    private static final int PAGE_SIZE = 10;
    private static final int MAX_VISIBLE_PAGE_BUTTONS = 5;

    private final PurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
    private final UserDao userDao = new UserDao();
    private final ImportSiteDao importSiteDao = new ImportSiteDao();
    private final AlertMessage alertMessage = new AlertMessage();

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<OrderListRow> purchaseOrderTable;

    @FXML
    private TableColumn<OrderListRow, Void> selectColumn;

    @FXML
    private TableColumn<OrderListRow, String> orderIdColumn;

    @FXML
    private TableColumn<OrderListRow, String> siteColumn;

    @FXML
    private TableColumn<OrderListRow, String> creatorColumn;

    @FXML
    private TableColumn<OrderListRow, String> orderDateColumn;

    @FXML
    private TableColumn<OrderListRow, String> deliveryColumn;

    @FXML
    private TableColumn<OrderListRow, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> deliveryFilterComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label deliveredOrdersLabel;

    @FXML
    private Label deliveredOrdersHeaderLabel;

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

    private final ObservableList<OrderListRow> tableItems = FXCollections.observableArrayList();
    private final List<Button> pageButtons = new ArrayList<>();
    private List<OrderListRow> allRows = List.of();
    private int currentPage = 1;
    private boolean suppressFilterEvents;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AppSession.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) {
            deliveredOrdersHeaderLabel.setText("Đơn hàng đã xác nhận");
        }
        pageButtons.addAll(Arrays.asList(pageButton1, pageButton2, pageButton3, pageButton4, pageButton5));

        configureColumns();
        configurePaginationControls();
        configureFilters();

        purchaseOrderTable.setItems(tableItems);
        loadData();
    }

    @FXML
    private void handleRefresh() {
        suppressFilterEvents = true;
        try {
            searchField.clear();
            statusFilterComboBox.getSelectionModel().selectFirst();
            deliveryFilterComboBox.getSelectionModel().selectFirst();
            fromDatePicker.setValue(null);
            toDatePicker.setValue(null);

            purchaseOrderTable.getSelectionModel().clearSelection();
        } finally {
            suppressFilterEvents = false;
        }

        loadData();
    }

    private void configureColumns() {
        selectColumn.setCellFactory(col -> new TableCell<OrderListRow, Void>() {
            private final RadioButton radio = new RadioButton();
            {
                radio.setOnMouseClicked(event -> {
                    if (getIndex() >= getTableView().getItems().size()) return;

                    OrderListRow row = getTableView().getItems().get(getIndex());
                    AppSession session = AppSession.getInstance();

                    if (session.getSelectedOrderId() != null && session.getSelectedOrderId().equals(row.getOrderId())) {
                        session.clearSelectedOrder();
                        getTableView().getSelectionModel().clearSelection();
                    } else {
                        session.setSelectedOrderId(row.getOrderId());
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
                    OrderListRow row = getTableView().getItems().get(getIndex());

                    Integer globalSelectedId = AppSession.getInstance().getSelectedOrderId();
                    boolean isSelected = (globalSelectedId != null && globalSelectedId.equals(row.getOrderId()));

                    radio.setSelected(isSelected);
                    if (isSelected) {
                        getTableView().getSelectionModel().select(getIndex());
                    }
                }
            }
        });
        
        orderIdColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getOrderIdDisplay()));
        siteColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSiteCode() + " - " + cellData.getValue().siteName()));
        creatorColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().creatorName()));
        orderDateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getOrderDateDisplay()));
        deliveryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDeliveryMeansLabel()));
        statusColumn.setCellValueFactory(cellData -> {
            OrderListRow row = cellData.getValue();
            if (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) {
                PurchaseOrderStatus status = row.getStatus();
                if (status == PurchaseOrderStatus.CONFIRMED) {
                    return new ReadOnlyStringWrapper("Chưa xác nhận");
                } else if (status == PurchaseOrderStatus.DELIVERED) {
                    return new ReadOnlyStringWrapper("Đã xác nhận");
                }
            }
            return new ReadOnlyStringWrapper(row.getStatusLabel());
        });
    }

    private void configurePaginationControls() {
        refreshButton.setOnAction(event -> handleRefresh());
        pagePrevButton.setOnAction(event -> goToPage(currentPage - 1));
        pageNextButton.setOnAction(event -> goToPage(currentPage + 1));
    }

    private void configureFilters() {
        suppressFilterEvents = true;
        try {
            if (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) {
                statusFilterComboBox.setItems(FXCollections.observableArrayList(
                        "Tất cả",
                        "Chưa xác nhận",
                        "Đã xác nhận"
                ));
            } else {
                statusFilterComboBox.setItems(FXCollections.observableArrayList(
                        "Tất cả",
                        "Bản nháp",
                        "Đã gửi",
                        "Xác nhận",
                        "Đã giao",
                        "Đã hủy"
                ));
            }
            statusFilterComboBox.getSelectionModel().selectFirst();

            deliveryFilterComboBox.setItems(FXCollections.observableArrayList(
                    "Tất cả",
                    "Đường biển (Tàu)",
                    "Đường hàng không (Máy bay)"
            ));
            deliveryFilterComboBox.getSelectionModel().selectFirst();
        } finally {
            suppressFilterEvents = false;
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        deliveryFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
        toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
    }

    private void loadData() {
        try {
            List<PurchaseOrder> orders;
            if (currentUser != null && currentUser.getRole() == UserRole.OVERSEAS_ORDER) {
                orders = purchaseOrderDao.findByCreatedBy(currentUser.getUserId());
            } else {
                orders = purchaseOrderDao.findAll();
            }

            if (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) {
                orders = orders.stream()
                        .filter(o -> o.getStatus() == PurchaseOrderStatus.CONFIRMED || o.getStatus() == PurchaseOrderStatus.DELIVERED)
                        .collect(Collectors.toList());
            }

            Map<Integer, String> creatorCache = new HashMap<>();
            Map<String, String> siteCache = new HashMap<>();
            List<OrderListRow> rows = new ArrayList<>();

            for (PurchaseOrder order : orders) {
                rows.add(new OrderListRow(
                        order, 
                        resolveCreatorName(order.getCreatedBy(), creatorCache), 
                        resolveSiteName(order.getSiteCode(), siteCache)
                ));
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
            alertMessage.errorMessage("Không thể tải danh sách đơn hàng: " + exception.getMessage());
        }
    }

    private void applyFiltersAndRender() {
        if (suppressFilterEvents) {
            return;
        }

        currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        List<OrderListRow> filteredRows = filterRows();
        int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = Math.max(0, (currentPage - 1) * PAGE_SIZE);
        int toIndex = Math.min(filteredRows.size(), fromIndex + PAGE_SIZE);

        tableItems.setAll(filteredRows.subList(fromIndex, toIndex));
        updatePaginationState(totalPages, filteredRows.size());
    }

    private List<OrderListRow> filterRows() {
        String keyword = normalize(searchField.getText());
        String selectedStatus = statusFilterComboBox.getValue();
        String selectedDelivery = deliveryFilterComboBox.getValue();
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
                .filter(row -> matchesDelivery(row, selectedDelivery))
                .filter(row -> matchesDateRange(row, startDate, endDate))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(OrderListRow row, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        UserRole role = currentUser != null ? currentUser.getRole() : null;
        return row.getSearchText(role).contains(keyword);
    }

    private boolean matchesStatus(OrderListRow row, String selectedStatus) {
        if (selectedStatus == null || selectedStatus.isBlank() || "Tất cả".equals(selectedStatus)) {
            return true;
        }

        if (currentUser != null && currentUser.getRole() == UserRole.WAREHOUSE) {
            PurchaseOrderStatus status = row.getStatus();
            if ("Chưa xác nhận".equalsIgnoreCase(selectedStatus)) {
                return status == PurchaseOrderStatus.CONFIRMED;
            } else if ("Đã xác nhận".equalsIgnoreCase(selectedStatus)) {
                return status == PurchaseOrderStatus.DELIVERED;
            }
            return false;
        }

        return selectedStatus.equalsIgnoreCase(row.getStatusLabel());
    }

    private boolean matchesDelivery(OrderListRow row, String selectedDelivery) {
        if (selectedDelivery == null || selectedDelivery.isBlank() || "Tất cả".equals(selectedDelivery)) {
            return true;
        }

        return selectedDelivery.equalsIgnoreCase(row.getDeliveryMeansLabel());
    }

    private boolean matchesDateRange(OrderListRow row, LocalDate startDate, LocalDate endDate) {
        LocalDate rowDate = row.getOrderDate();
        if (rowDate == null) {
            return false;
        }

        if (startDate != null && rowDate.isBefore(startDate)) {
            return false;
        }

        return endDate == null || !rowDate.isAfter(endDate);
    }

    private void updateSummaryLabels(List<OrderListRow> rows) {
        totalOrdersLabel.setText(String.valueOf(rows.size()));
        deliveredOrdersLabel.setText(String.valueOf(rows.stream().filter(row -> row.getStatus() == PurchaseOrderStatus.DELIVERED).count()));
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
            
            // Highlight active page
            button.getStyleClass().remove("pagination-page-active");
            if (pageNumber == currentPage) {
                button.getStyleClass().add("pagination-page-active");
            }

            int targetPage = pageNumber;
            button.setOnAction(event -> goToPage(targetPage));
        }

        if (itemCount == 0) {
            pageInfoLabel.setText("Trang 1 / 1");
        }
    }

    private void goToPage(int targetPage) {
        List<OrderListRow> filteredRows = filterRows();
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
                    .map(user -> {
                        String fullName = user.getFullName();
                        if (fullName != null && !fullName.isBlank()) return fullName;
                        return user.getUsername() == null || user.getUsername().isBlank() ? "Không xác định" : user.getUsername();
                    })
                    .orElse("Người dùng #" + userId);
            cache.put(userId, creatorName);
            return creatorName;
        } catch (SQLException exception) {
            String fallback = "Người dùng #" + userId;
            cache.put(userId, fallback);
            return fallback;
        }
    }

    private String resolveSiteName(String siteCode, Map<String, String> cache) {
        if (siteCode == null) {
            return "Không xác định";
        }

        if (cache.containsKey(siteCode)) {
            return cache.get(siteCode);
        }

        try {
            String siteName = importSiteDao.findById(siteCode)
                    .map(ImportSite::getSiteName)
                    .orElse("Không xác định");
            cache.put(siteCode, siteName);
            return siteName;
        } catch (SQLException exception) {
            String fallback = "Không xác định";
            cache.put(siteCode, fallback);
            return fallback;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
