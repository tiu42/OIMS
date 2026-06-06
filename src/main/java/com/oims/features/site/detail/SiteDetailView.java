package com.oims.features.site.detail;

import com.oims.core.util.AlertMessage;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class SiteDetailView implements Initializable {

    @FXML
    private Label pageTitle;

    @FXML
    private Label siteCodeLabel;

    @FXML
    private Label siteNameLabel;

    @FXML
    private Label countryLabel;

    @FXML
    private Label contactInfoLabel;

    @FXML
    private TableView<SiteMerchandiseRow> merchandiseTable;

    @FXML
    private TableColumn<SiteMerchandiseRow, String> merchCodeColumn;

    @FXML
    private TableColumn<SiteMerchandiseRow, String> merchNameColumn;

    @FXML
    private TableColumn<SiteMerchandiseRow, String> stockQuantityColumn;

    @FXML
    private TableColumn<SiteMerchandiseRow, String> stockUnitColumn;

    @FXML
    private TableColumn<SiteMerchandiseRow, String> stockUpdatedColumn;

    @FXML
    private TableView<SiteTransportRow> transportTable;

    @FXML
    private TableColumn<SiteTransportRow, String> shipDaysColumn;

    @FXML
    private TableColumn<SiteTransportRow, String> airDaysColumn;

    @FXML
    private TableColumn<SiteTransportRow, String> otherInfoColumn;

    @FXML
    private TableColumn<SiteTransportRow, String> updatedDateColumn;

    @FXML
    private Button backToListBtn;

    private final SiteDetailController controller = new SiteDetailController();
    private final AlertMessage alertMessage = new AlertMessage();
    private final ObservableList<SiteMerchandiseRow> merchandiseRows = FXCollections.observableArrayList();
    private final ObservableList<SiteTransportRow> transportRows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureMerchandiseColumns();
        configureTransportColumns();
        merchandiseTable.setItems(merchandiseRows);
        transportTable.setItems(transportRows);
        backToListBtn.setOnAction(event -> navigateToList());
        loadData();
    }

    private void configureMerchandiseColumns() {
        merchCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getMerchandiseCode()));
        merchNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getMerchandiseName()));
        stockQuantityColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getInStockQuantity()));
        stockUnitColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUnit()));
        stockUpdatedColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStockUpdatedDate()));
    }

    private void configureTransportColumns() {
        shipDaysColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getShipDays()));
        airDaysColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAirDays()));
        otherInfoColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getOtherInfo()));
        updatedDateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUpdatedDate()));
    }

    private void loadData() {
        try {
            Optional<SiteDetailDTO> siteOpt = controller.loadSiteData();
            if (siteOpt.isEmpty()) {
                pageTitle.setText("Chi tiết site đối tác");
                siteCodeLabel.setText("—");
                siteNameLabel.setText("—");
                countryLabel.setText("—");
                contactInfoLabel.setText("—");
                merchandiseRows.clear();
                transportRows.clear();
                alertMessage.errorMessage("Chưa có site nào được chọn hoặc site không tồn tại. Hãy chọn site từ danh sách.");
                return;
            }

            SiteDetailDTO site = siteOpt.get();
            pageTitle.setText("Chi tiết site: " + site.getSiteCode());
            siteCodeLabel.setText(site.getSiteCode());
            siteNameLabel.setText(site.getSiteName());
            countryLabel.setText(site.getCountry());
            contactInfoLabel.setText(site.getContactInfo().isBlank() ? "—" : site.getContactInfo());
            merchandiseRows.setAll(controller.loadMerchandiseData());
            transportRows.setAll(controller.loadTransportData());
        } catch (SQLException exception) {
            alertMessage.errorMessage("Không thể tải chi tiết site: " + exception.getMessage());
        }
    }

    private void navigateToList() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/site/list-site-view.fxml"));
            StackPane contentArea = (StackPane) backToListBtn.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException exception) {
            alertMessage.errorMessage("Không thể quay lại danh sách site: " + exception.getMessage());
        }
    }
}
