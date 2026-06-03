package com.oims.core.model;

public enum UserRole {
    ADMIN("admin", "Admin"),
    SALES("sales", "NV.BP.Bán hàng"),
    OVERSEAS_ORDER("overseas_order", "NV.BP.Đặt hàng"),
    WAREHOUSE("warehouse", "NV.BP.Quản lý kho");

    private final String dbValue;
    private final String displayName;

    UserRole(String dbValue, String displayName) {
        this.dbValue = dbValue;
        this.displayName = displayName;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static UserRole fromDbValue(String value) {
        for (UserRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}