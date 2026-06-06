package com.oims.features.site.detail;

import com.oims.core.model.SiteMerchandise;

import java.time.format.DateTimeFormatter;

public final class SiteMerchandiseRow {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String merchandiseCode;
    private final String merchandiseName;
    private final String inStockQuantity;
    private final String unit;
    private final String stockUpdatedDate;

    public SiteMerchandiseRow(String merchandiseCode, String merchandiseName, SiteMerchandise stock) {
        this.merchandiseCode = merchandiseCode;
        this.merchandiseName = merchandiseName == null ? "" : merchandiseName;
        this.inStockQuantity = String.valueOf(stock.getInStockQuantity());
        this.unit = stock.getUnit() == null ? "" : stock.getUnit();
        this.stockUpdatedDate = stock.getStockUpdatedDate() == null
                ? ""
                : stock.getStockUpdatedDate().format(DATE_FORMAT);
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public String getMerchandiseName() {
        return merchandiseName;
    }

    public String getInStockQuantity() {
        return inStockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getStockUpdatedDate() {
        return stockUpdatedDate;
    }
}
