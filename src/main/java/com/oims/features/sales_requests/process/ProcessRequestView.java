package com.oims.features.sales_requests.process;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.User;
import com.oims.core.dao.MerchandiseDao;
import com.oims.core.session.AppSession;
import com.oims.core.util.AlertMessage;
import com.oims.features.sales_requests.process.ProcessRequestController.*;
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
import java.util.stream.Collectors;

public class ProcessRequestView implements Initializable {

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
    private TableView<ItemDemand> itemsTable;

    @FXML
    private TableColumn<ItemDemand, String> itemCodeCol;

    @FXML
    private TableColumn<ItemDemand, String> itemNameCol;

    @FXML
    private TableColumn<ItemDemand, String> itemQtyCol;

    @FXML
    private TableColumn<ItemDemand, String> itemUnitCol;

    @FXML
    private TableView<SiteStockTransportDTO> siteStockTable;

    @FXML
    private TableColumn<SiteStockTransportDTO, String> siteCodeCol;

    @FXML
    private TableColumn<SiteStockTransportDTO, String> siteNameCol;

    @FXML
    private TableColumn<SiteStockTransportDTO, String> siteStockCol;

    @FXML
    private TableColumn<SiteStockTransportDTO, String> siteShipDaysCol;

    @FXML
    private TableColumn<SiteStockTransportDTO, String> siteAirDaysCol;

    @FXML
    private ComboBox<String> prefSiteCombo;

    @FXML
    private ComboBox<String> nonPrefSiteCombo;

    @FXML
    private ComboBox<String> prefDeliveryCombo;

    @FXML
    private Button setDefaultBtn;

    @FXML
    private Label configStatusLabel;

    @FXML
    private Button generatePlansBtn;

    @FXML
    private Button cancelProcessBtn;

    private final ProcessRequestController controller = new ProcessRequestController();
    private final AlertMessage alertMessage = new AlertMessage();
    private final ObservableList<ItemDemand> demandsList = FXCollections.observableArrayList();
    private final ObservableList<SiteStockTransportDTO> siteStockList = FXCollections.observableArrayList();

    private Integer requestId;
    private final Map<String, ItemConfig> configs = new HashMap<>();
    private final Set<String> skippedItems = new HashSet<>();
    private final Map<String, List<SiteStockTransportDTO>> cachedSiteStocks = new HashMap<>();
    private boolean hasErrors = false;
    private boolean suppressConfigChangeEvents = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        requestId = AppSession.getInstance().getSelectedRequestId();
        if (requestId == null) {
            alertMessage.errorMessage("Không tìm thấy mã yêu cầu cần xử lý.");
            navigateBackToDetail();
            return;
        }

        initTableColumns();
        if (!tryBeginProcessing()) {
            return;
        }
        loadRequestData();
        initEventHandlers();

        itemsTable.setItems(demandsList);
        siteStockTable.setItems(siteStockList);
    }

    private boolean tryBeginProcessing() {
        try {
            if (!controller.beginProcessing(requestId)) {
                alertMessage.errorMessage("Không thể xử lý yêu cầu này. Yêu cầu có thể đã được xử lý xong hoặc không còn ở trạng thái Chờ xử lý.");
                navigateBackToDetail(false);
                return false;
            }
            return true;
        } catch (SQLException e) {
            alertMessage.errorMessage("Không thể bắt đầu xử lý yêu cầu: " + e.getMessage());
            navigateBackToDetail(false);
            return false;
        }
    }

    private void initTableColumns() {
        // Items Table
        itemCodeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseCode()));
        itemNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().merchandiseName()));
        itemQtyCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().quantity())));
        itemUnitCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().unit()));

        // Site Stock Table
        siteCodeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().siteCode()));
        siteNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().siteName()));
        siteStockCol.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%,d", cell.getValue().inStock())));
        siteShipDaysCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().shipDays() > 0 ? cell.getValue().shipDays() + " ngày" : "Không hỗ trợ"));
        siteAirDaysCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().airDays() > 0 ? cell.getValue().airDays() + " ngày" : "Không hỗ trợ"));
    }

    private void loadRequestData() {
        try {
            Optional<SalesRequest> requestOpt = controller.getSalesRequest(requestId);
            if (requestOpt.isEmpty()) {
                alertMessage.errorMessage("Yêu cầu nhập hàng không tồn tại.");
                navigateBackToDetail();
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

            // Load items
            List<SalesRequestItem> items = controller.getSalesRequestItems(requestId);
            List<Merchandise> merchandises = new MerchandiseDao().findAll();
            Map<String, String> merchNameMap = merchandises.stream()
                    .collect(Collectors.toMap(Merchandise::getMerchandiseCode, Merchandise::getMerchandiseName));

            demandsList.clear();
            for (SalesRequestItem item : items) {
                String name = merchNameMap.getOrDefault(item.getMerchandiseCode(), "Không xác định");
                demandsList.add(new ItemDemand(
                        item.getMerchandiseCode(),
                        name,
                        item.getQuantityOrdered(),
                        item.getUnit()
                ));
            }

        } catch (SQLException e) {
            alertMessage.errorMessage("Lỗi tải thông tin yêu cầu: " + e.getMessage());
            navigateBackToDetail();
        }
    }

    private void initEventHandlers() {
        // Selection of item in table
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleItemSelection(newVal);
            }
        });

        // Preference configuration changes
        prefSiteCombo.valueProperty().addListener((obs, oldVal, newVal) -> saveCurrentItemConfig());
        nonPrefSiteCombo.valueProperty().addListener((obs, oldVal, newVal) -> saveCurrentItemConfig());
        prefDeliveryCombo.valueProperty().addListener((obs, oldVal, newVal) -> saveCurrentItemConfig());

        // Default button
        setDefaultBtn.setOnAction(event -> handleSetDefault());

        // Generate plans button
        generatePlansBtn.setOnAction(event -> handleGeneratePlans());

        // Cancel
        cancelProcessBtn.setOnAction(event -> handleCancel());
    }

    private void handleItemSelection(ItemDemand item) {
        try {
            // Load and cache site stock
            List<SiteStockTransportDTO> siteStocks = cachedSiteStocks.get(item.merchandiseCode());
            if (siteStocks == null) {
                siteStocks = controller.getSiteStockAndTransport(item.merchandiseCode());
                cachedSiteStocks.put(item.merchandiseCode(), siteStocks);
            }
            siteStockList.setAll(siteStocks);

            suppressConfigChangeEvents = true;
            try {
                // Populate Site Combo Boxes
                ObservableList<String> siteCodes = FXCollections.observableArrayList();
                siteCodes.add("[Không chọn]");
                for (SiteStockTransportDTO s : siteStocks) {
                    siteCodes.add(s.siteCode());
                }
                prefSiteCombo.setItems(siteCodes);
                nonPrefSiteCombo.setItems(siteCodes);

                // Populate Delivery Combo Box
                prefDeliveryCombo.setItems(FXCollections.observableArrayList(
                        "[Không chọn]",
                        "Đường biển (Tàu)",
                        "Đường hàng không (Máy bay)"
                ));

                // Load existing configuration or defaults
                ItemConfig config = configs.get(item.merchandiseCode());
                if (config != null) {
                    prefSiteCombo.setValue(config.preferredSite() == null ? "[Không chọn]" : config.preferredSite());
                    nonPrefSiteCombo.setValue(config.nonPreferredSite() == null ? "[Không chọn]" : config.nonPreferredSite());
                    
                    if (config.preferredDelivery() == DeliveryMeans.SHIP_DELIVERY) {
                        prefDeliveryCombo.setValue("Đường biển (Tàu)");
                    } else if (config.preferredDelivery() == DeliveryMeans.AIR_DELIVERY) {
                        prefDeliveryCombo.setValue("Đường hàng không (Máy bay)");
                    } else {
                        prefDeliveryCombo.setValue("[Không chọn]");
                    }
                    configStatusLabel.setText("Trạng thái: Đã cấu hình");
                    configStatusLabel.getStyleClass().setAll("config-status-configured");
                } else {
                    // Defaults
                    prefSiteCombo.setValue("[Không chọn]");
                    nonPrefSiteCombo.setValue("[Không chọn]");
                    prefDeliveryCombo.setValue("Đường biển (Tàu)"); // default is ship/sea
                    
                    configStatusLabel.setText("Trạng thái: Mặc định");
                    configStatusLabel.getStyleClass().setAll("config-status-default");
                }
            } finally {
                suppressConfigChangeEvents = false;
            }

        } catch (SQLException e) {
            alertMessage.errorMessage("Lỗi tải thông tin site: " + e.getMessage());
        }
    }

    private void saveCurrentItemConfig() {
        if (suppressConfigChangeEvents) return;

        ItemDemand selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        String prefSite = prefSiteCombo.getValue();
        String nonPrefSite = nonPrefSiteCombo.getValue();
        String deliveryText = prefDeliveryCombo.getValue();

        String realPrefSite = (prefSite == null || "[Không chọn]".equals(prefSite)) ? null : prefSite;
        String realNonPrefSite = (nonPrefSite == null || "[Không chọn]".equals(nonPrefSite)) ? null : nonPrefSite;
        
        DeliveryMeans dm = null;
        if ("Đường biển (Tàu)".equals(deliveryText)) {
            dm = DeliveryMeans.SHIP_DELIVERY;
        } else if ("Đường hàng không (Máy bay)".equals(deliveryText)) {
            dm = DeliveryMeans.AIR_DELIVERY;
        }

        // Validate
        if (realPrefSite != null && realPrefSite.equals(realNonPrefSite)) {
            alertMessage.errorMessage("Site ưu tiên và Site không ưu tiên không được trùng nhau.");
            // Revert pref combo to none
            suppressConfigChangeEvents = true;
            prefSiteCombo.setValue("[Không chọn]");
            suppressConfigChangeEvents = false;
            return;
        }

        configs.put(selectedItem.merchandiseCode(), new ItemConfig(realPrefSite, realNonPrefSite, dm));
        configStatusLabel.setText("Trạng thái: Đã cấu hình");
        configStatusLabel.getStyleClass().setAll("config-status-configured");
    }

    private void handleSetDefault() {
        ItemDemand selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        suppressConfigChangeEvents = true;
        try {
            prefSiteCombo.setValue("[Không chọn]");
            nonPrefSiteCombo.setValue("[Không chọn]");
            prefDeliveryCombo.setValue("Đường biển (Tàu)");
            
            configs.put(selectedItem.merchandiseCode(), new ItemConfig(null, null, DeliveryMeans.SHIP_DELIVERY));
            configStatusLabel.setText("Trạng thái: Mặc định");
            configStatusLabel.getStyleClass().setAll("config-status-default");
        } finally {
            suppressConfigChangeEvents = false;
        }
    }

    private void handleGeneratePlans() {
        if (demandsList.isEmpty()) return;

        try {
            // Check for insufficient or missing site stock for each item
            List<ItemDemand> failed = controller.getFailedDemands(demandsList);
            
            // Show alert warning for each failed item
            for (ItemDemand f : failed) {
                if (!skippedItems.contains(f.merchandiseCode())) {
                    List<SiteStockTransportDTO> sites = cachedSiteStocks.get(f.merchandiseCode());
                    if (sites == null) {
                        sites = controller.getSiteStockAndTransport(f.merchandiseCode());
                        cachedSiteStocks.put(f.merchandiseCode(), sites);
                    }
                    int totalStock = sites.stream().mapToInt(SiteStockTransportDTO::inStock).sum();

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Cảnh báo thiếu hàng");
                    alert.setHeaderText("Mặt hàng không đủ tồn kho cung cấp");
                    alert.setContentText("Mặt hàng: " + f.merchandiseName() + " (Mã: " + f.merchandiseCode() + ")\n" +
                            "Yêu cầu: " + f.quantity() + " " + f.unit() + ".\n" +
                            "Tổng tồn kho khả dụng tại các site đối tác: " + totalStock + " " + f.unit() + ".\n\n" +
                            "Vui lòng ghi nhận lỗi cho mặt hàng này để bỏ qua và tiếp tục lên phương án cho các mặt hàng khác.");
                    
                    ButtonType recordErrorBtn = new ButtonType("Ghi nhận lỗi", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelBtn = new ButtonType("Hủy bỏ", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(recordErrorBtn, cancelBtn);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == recordErrorBtn) {
                        skippedItems.add(f.merchandiseCode());
                        hasErrors = true;
                    } else {
                        alertMessage.errorMessage("Hủy bỏ thao tác lập phương án.");
                        return;
                    }
                }
            }

            // Filter out skipped items
            List<ItemDemand> activeDemands = demandsList.stream()
                    .filter(d -> !skippedItems.contains(d.merchandiseCode()))
                    .collect(Collectors.toList());

            if (activeDemands.isEmpty()) {
                alertMessage.errorMessage("Tất cả các mặt hàng đều bị lỗi/thiếu hàng. Không thể lập phương án.");
                return;
            }

            // Populate default configurations for any item that doesn't have custom configs
            for (ItemDemand d : activeDemands) {
                if (!configs.containsKey(d.merchandiseCode())) {
                    configs.put(d.merchandiseCode(), new ItemConfig(null, null, DeliveryMeans.SHIP_DELIVERY));
                }
            }

            // Generate plans
            List<PlanDTO> plans = controller.generatePlans(activeDemands, configs, cachedSiteStocks);
            if (plans.isEmpty()) {
                alertMessage.errorMessage("Không tìm thấy phương án phân bổ khả thi nào.");
                return;
            }

            // Successfully generated, load plans view and pass state
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oims/features/sales_requests/plans-sales-request-view.fxml"));
                Parent view = loader.load();

                PlansRequestView plansView = loader.getController();
                plansView.setPlansData(plans, configs, skippedItems, hasErrors);

                StackPane contentArea = (StackPane) pageTitle.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(view);
                }
            } catch (IOException e) {
                alertMessage.errorMessage("Không thể mở màn hình phương án: " + e.getMessage());
            }

        } catch (SQLException e) {
            alertMessage.errorMessage("Lỗi khi lập phương án: " + e.getMessage());
        }
    }

    public void restoreState(Map<String, ItemConfig> savedConfigs, Set<String> savedSkippedItems, boolean savedHasErrors) {
        this.configs.putAll(savedConfigs);
        this.skippedItems.addAll(savedSkippedItems);
        this.hasErrors = savedHasErrors;

        // Select first item if any to populate site details and configs
        if (!demandsList.isEmpty()) {
            itemsTable.getSelectionModel().selectFirst();
        }
    }

    private void handleCancel() {
        navigateBackToDetail(true);
    }

    private void navigateBackToDetail() {
        navigateBackToDetail(true);
    }

    private void navigateBackToDetail(boolean releaseProcessingLock) {
        if (releaseProcessingLock && requestId != null) {
            try {
                controller.cancelProcessing(requestId);
            } catch (SQLException e) {
                alertMessage.errorMessage("Không thể hủy trạng thái xử lý yêu cầu: " + e.getMessage());
                return;
            }
        }

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
}
