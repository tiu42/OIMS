package com.oims.features.purchase_order.list;

import com.oims.core.model.PurchaseOrder;
import com.oims.core.model.PurchaseOrderStatus;
import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.UserRole;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record OrderListRow(PurchaseOrder purchaseOrder, String creatorName, String siteName) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Integer getOrderId() {
        return purchaseOrder == null ? null : purchaseOrder.getOrderId();
    }

    public Integer getRequestId() {
        return purchaseOrder == null ? null : purchaseOrder.getRequestId();
    }

    public String getSiteCode() {
        return purchaseOrder == null ? null : purchaseOrder.getSiteCode();
    }

    public Integer getCreatedBy() {
        return purchaseOrder == null ? null : purchaseOrder.getCreatedBy();
    }

    public LocalDate getOrderDate() {
        return purchaseOrder == null ? null : purchaseOrder.getOrderDate();
    }

    public String getOrderDateDisplay() {
        LocalDate orderDate = getOrderDate();
        return orderDate == null ? "" : orderDate.format(DATE_FORMATTER);
    }

    public DeliveryMeans getDeliveryMeans() {
        return purchaseOrder == null ? null : purchaseOrder.getDeliveryMeans();
    }

    public String getDeliveryMeansLabel() {
        DeliveryMeans dm = getDeliveryMeans();
        if (dm == null) return "Không xác định";
        return dm == DeliveryMeans.SHIP_DELIVERY ? "Đường biển (Tàu)" : "Đường hàng không (Máy bay)";
    }

    public PurchaseOrderStatus getStatus() {
        return purchaseOrder == null ? null : purchaseOrder.getStatus();
    }

    public String getStatusLabel() {
        PurchaseOrderStatus status = getStatus();
        if (status == null) {
            return "Không xác định";
        }
        return switch (status) {
            case DRAFT -> "Bản nháp";
            case SENT -> "Đã gửi";
            case CONFIRMED -> "Xác nhận";
            case DELIVERED -> "Đã giao";
            case CANCELLED -> "Đã hủy";
        };
    }

    public String getOrderIdDisplay() {
        Integer orderId = getOrderId();
        return orderId == null ? "" : String.valueOf(orderId);
    }

    public String getRequestIdDisplay() {
        Integer requestId = getRequestId();
        return requestId == null ? "" : String.valueOf(requestId);
    }

    public String getSearchText(UserRole role) {
        String statusLabel = getStatusLabel();
        if (role == UserRole.WAREHOUSE) {
            PurchaseOrderStatus status = getStatus();
            if (status == PurchaseOrderStatus.SENT || status == PurchaseOrderStatus.CONFIRMED) {
                statusLabel = "Chưa xác nhận";
            } else if (status == PurchaseOrderStatus.DELIVERED) {
                statusLabel = "Đã xác nhận";
            }
        }
        return String.join(" ",
                safeText(getOrderIdDisplay()),
                safeText(getRequestIdDisplay()),
                safeText(getSiteCode()),
                safeText(siteName),
                safeText(creatorName),
                safeText(getOrderDateDisplay()),
                safeText(getDeliveryMeansLabel()),
                safeText(statusLabel)
        ).toLowerCase();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
