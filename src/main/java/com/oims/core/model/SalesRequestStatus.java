package com.oims.core.model;

public enum SalesRequestStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    ERROR("error");

    private final String dbValue;

    SalesRequestStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static SalesRequestStatus fromDbValue(String value) {
        for (SalesRequestStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown sales request status: " + value);
    }
}