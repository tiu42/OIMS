package com.oims.core.model;

public class PurchaseOrderItem {
    private Integer orderItemId;
    private Integer orderId;
    private String merchandiseCode;
    private Integer quantityOrdered;
    private String unit;

    public PurchaseOrderItem() {
    }

    public PurchaseOrderItem(Integer orderItemId, Integer orderId, String merchandiseCode, Integer quantityOrdered, String unit) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.merchandiseCode = merchandiseCode;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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
}