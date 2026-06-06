package com.oims.features.site.list;

import com.oims.core.dao.ImportSiteDao;
import com.oims.core.model.ImportSite;
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
import java.util.*;
import java.util.stream.Collectors;

public class SiteListController implements Initializable {

    private static final int PAGE_SIZE = 10;
    private static final int MAX_VISIBLE_PAGE_BUTTONS = 5;

    private final ImportSiteDao importSiteDao = new ImportSiteDao();
    private final AlertMessage alertMessage = new AlertMessage();

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<SiteListRow> siteTable;

    @FXML
    private TableColumn<SiteListRow, Void> selectColumn;

    @FXML
    private TableColumn<SiteListRow, String> siteCodeColumn;

    @FXML
    private TableColumn<SiteListRow, String> siteNameColumn;

    @FXML
    private TableColumn<SiteListRow, String> countryColumn;

    @FXML
    private TableColumn<SiteListRow, String> contactColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalSitesLabel;

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

    private final ObservableList<SiteListRow> tableItems = FXCollections.observableArrayList();
    private final List<Button> pageButtons = new ArrayList<>();
    private List<SiteListRow> allRows = List.of();
    private int currentPage = 1;
    private boolean suppressFilterEvents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageButtons.addAll(Arrays.asList(pageButton1, pageButton2, pageButton3, pageButton4, pageButton5));

        configureColumns();
        configureRowDoubleClick();
        configurePaginationControls();
        configureFilters();

        siteTable.setItems(tableItems);
        loadData();
    }

    @FXML
    private void handleRefresh() {
        suppressFilterEvents = true;
        try {
            searchField.clear();
            siteTable.getSelectionModel().clearSelection();
        } finally {
            suppressFilterEvents = false;
        }
        loadData();
    }

    private void configureColumns() {
        selectColumn.setCellFactory(col -> new TableCell<SiteListRow, Void>() {
            private final RadioButton radio = new RadioButton();

            {
                radio.setOnMouseClicked(event -> {
                    if (getIndex() >= getTableView().getItems().size()) {
                        return;
                    }

                    SiteListRow row = getTableView().getItems().get(getIndex());
                    AppSession session = AppSession.getInstance();
                    String siteCode = row.getSiteCode();

                    if (siteCode != null && siteCode.equals(session.getSelectedSiteCode())) {
                        session.clearSelectedSite();
                        getTableView().getSelectionModel().clearSelection();
                    } else {
                        session.setSelectedSiteCode(siteCode);
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
                    return;
                }

                setGraphic(radio);
                SiteListRow row = getTableView().getItems().get(getIndex());
                String globalSelectedCode = AppSession.getInstance().getSelectedSiteCode();
                boolean isSelected = globalSelectedCode != null && globalSelectedCode.equals(row.getSiteCode());
                radio.setSelected(isSelected);
                if (isSelected) {
                    getTableView().getSelectionModel().select(getIndex());
                }
            }
        });

        siteCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSiteCode()));
        siteNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSiteName()));
        countryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCountry()));
        contactColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getContactInfo()));
    }

    private void configureRowDoubleClick() {
        siteTable.setRowFactory(table -> {
            TableRow<SiteListRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty() || event.getClickCount() != 2) {
                    return;
                }
                openSiteDetail(row.getItem());
            });
            return row;
        });
    }

    private void openSiteDetail(SiteListRow row) {
        if (row == null || row.getSiteCode() == null) {
            return;
        }

        AppSession.getInstance().setSelectedSiteCode(row.getSiteCode());
        siteTable.getSelectionModel().select(row);
        siteTable.refresh();

        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/oims/features/site/detail-site-view.fxml"));
            StackPane contentArea = (StackPane) siteTable.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            }
        } catch (IOException e) {
            alertMessage.errorMessage("Không thể mở màn hình chi tiết site: " + e.getMessage());
        }
    }

    private void configurePaginationControls() {
        refreshButton.setOnAction(event -> handleRefresh());
        pagePrevButton.setOnAction(event -> goToPage(currentPage - 1));
        pageNextButton.setOnAction(event -> goToPage(currentPage + 1));
    }

    private void configureFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFiltersAndRender());
    }

    private void loadData() {
        try {
            List<SiteListRow> rows = new ArrayList<>();
            for (ImportSite site : importSiteDao.findAll()) {
                rows.add(new SiteListRow(site));
            }
            allRows = rows;
            totalSitesLabel.setText(String.valueOf(allRows.size()));
            currentPage = 1;
            renderPage();
        } catch (SQLException exception) {
            allRows = List.of();
            tableItems.clear();
            totalSitesLabel.setText("0");
            updatePaginationState(0, 0);
            alertMessage.errorMessage("Không thể tải danh sách site: " + exception.getMessage());
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
        List<SiteListRow> filteredRows = filterRows();
        int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) PAGE_SIZE));
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = Math.max(0, (currentPage - 1) * PAGE_SIZE);
        int toIndex = Math.min(filteredRows.size(), fromIndex + PAGE_SIZE);
        tableItems.setAll(filteredRows.subList(fromIndex, toIndex));
        updatePaginationState(totalPages, filteredRows.size());
    }

    private List<SiteListRow> filterRows() {
        String keyword = normalize(searchField.getText());
        return allRows.stream()
                .filter(row -> keyword.isBlank() || row.getSearchText().contains(keyword))
                .collect(Collectors.toList());
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
        List<SiteListRow> filteredRows = filterRows();
        int totalPages = Math.max(1, (int) Math.ceil(filteredRows.size() / (double) PAGE_SIZE));
        currentPage = Math.max(1, Math.min(targetPage, totalPages));

        int fromIndex = Math.max(0, (currentPage - 1) * PAGE_SIZE);
        int toIndex = Math.min(filteredRows.size(), fromIndex + PAGE_SIZE);
        tableItems.setAll(filteredRows.subList(fromIndex, toIndex));
        updatePaginationState(totalPages, filteredRows.size());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
