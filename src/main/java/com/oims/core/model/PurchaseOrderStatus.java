package com.oims.core.model;

public enum PurchaseOrderStatus {
    DRAFT("draft"),
    SENT("sent"),
    CONFIRMED("confirmed"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String dbValue;

    PurchaseOrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static PurchaseOrderStatus fromDbValue(String value) {
        for (PurchaseOrderStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown purchase order status: " + value);
    }
}