package com.oims.core.model;

public enum DeliveryMeans {
    SHIP_DELIVERY("ship delivery"),
    AIR_DELIVERY("air delivery");

    private final String dbValue;

    DeliveryMeans(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static DeliveryMeans fromDbValue(String value) {
        for (DeliveryMeans means : values()) {
            if (means.dbValue.equalsIgnoreCase(value)) {
                return means;
            }
        }
        throw new IllegalArgumentException("Unknown delivery means: " + value);
    }
}