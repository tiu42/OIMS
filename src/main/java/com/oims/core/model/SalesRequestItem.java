package com.oims.core.model;

import java.time.LocalDate;

public class SalesRequestItem {
    private Integer itemId;
    private Integer requestId;
    private String merchandiseCode;
    private Integer quantityOrdered;
    private String unit;
    private LocalDate desiredDeliveryDate;

    public SalesRequestItem() {
    }

    public SalesRequestItem(Integer itemId, Integer requestId, String merchandiseCode, Integer quantityOrdered, String unit, LocalDate desiredDeliveryDate) {
        this.itemId = itemId;
        this.requestId = requestId;
        this.merchandiseCode = merchandiseCode;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.desiredDeliveryDate = desiredDeliveryDate;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public void setMerchandiseCode(String merchandiseCode) {
        this.merchandiseCode = merchandiseCode;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getDesiredDeliveryDate() {
        return desiredDeliveryDate;
    }

    public void setDesiredDeliveryDate(LocalDate desiredDeliveryDate) {
        this.desiredDeliveryDate = desiredDeliveryDate;
    }
}