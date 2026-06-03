package com.oims.core.model;

import java.time.LocalDate;

public class PurchaseOrder {
    private Integer orderId;
    private Integer requestId;
    private String siteCode;
    private Integer createdBy;
    private LocalDate orderDate;
    private DeliveryMeans deliveryMeans;
    private PurchaseOrderStatus status;

    public PurchaseOrder() {
    }

    public PurchaseOrder(Integer orderId, Integer requestId, String siteCode, Integer createdBy, LocalDate orderDate, DeliveryMeans deliveryMeans, PurchaseOrderStatus status) {
        this.orderId = orderId;
        this.requestId = requestId;
        this.siteCode = siteCode;
        this.createdBy = createdBy;
        this.orderDate = orderDate;
        this.deliveryMeans = deliveryMeans;
        this.status = status;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public DeliveryMeans getDeliveryMeans() {
        return deliveryMeans;
    }

    public void setDeliveryMeans(DeliveryMeans deliveryMeans) {
        this.deliveryMeans = deliveryMeans;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }
}