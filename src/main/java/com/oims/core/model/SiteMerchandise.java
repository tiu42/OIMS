package com.oims.core.model;

import java.time.LocalDate;

public class SiteMerchandise {
    private String siteCode;
    private String merchandiseCode;
    private Integer inStockQuantity;
    private String unit;
    private LocalDate stockUpdatedDate;

    public SiteMerchandise() {
    }

    public SiteMerchandise(String siteCode, String merchandiseCode, Integer inStockQuantity, String unit, LocalDate stockUpdatedDate) {
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.inStockQuantity = inStockQuantity;
        this.unit = unit;
        this.stockUpdatedDate = stockUpdatedDate;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public void setMerchandiseCode(String merchandiseCode) {
        this.merchandiseCode = merchandiseCode;
    }

    public Integer getInStockQuantity() {
        return inStockQuantity;
    }

    public void setInStockQuantity(Integer inStockQuantity) {
        this.inStockQuantity = inStockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getStockUpdatedDate() {
        return stockUpdatedDate;
    }

    public void setStockUpdatedDate(LocalDate stockUpdatedDate) {
        this.stockUpdatedDate = stockUpdatedDate;
    }
}