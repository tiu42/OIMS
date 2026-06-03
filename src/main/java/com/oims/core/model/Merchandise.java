package com.oims.core.model;

public class Merchandise {
    private String merchandiseCode;
    private String merchandiseName;
    private String defaultUnit;

    public Merchandise() {
    }

    public Merchandise(String merchandiseCode, String merchandiseName, String defaultUnit) {
        this.merchandiseCode = merchandiseCode;
        this.merchandiseName = merchandiseName;
        this.defaultUnit = defaultUnit;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public void setMerchandiseCode(String merchandiseCode) {
        this.merchandiseCode = merchandiseCode;
    }

    public String getMerchandiseName() {
        return merchandiseName;
    }

    public void setMerchandiseName(String merchandiseName) {
        this.merchandiseName = merchandiseName;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }
}