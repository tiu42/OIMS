package com.oims.core.model;

import java.time.LocalDate;

public class SiteTransportInfo {
    private Integer transportId;
    private String siteCode;
    private int shipDays;
    private int airDays;
    private String otherInfo;
    private LocalDate updatedDate;

    public SiteTransportInfo() {
    }

    public SiteTransportInfo(Integer transportId, String siteCode, int shipDays, int airDays, String otherInfo, LocalDate updatedDate) {
        this.transportId = transportId;
        this.siteCode = siteCode;
        this.shipDays = shipDays;
        this.airDays = airDays;
        this.otherInfo = otherInfo;
        this.updatedDate = updatedDate;
    }

    public Integer getTransportId() {
        return transportId;
    }

    public void setTransportId(Integer transportId) {
        this.transportId = transportId;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public int getShipDays() {
        return shipDays;
    }

    public void setShipDays(int shipDays) {
        this.shipDays = shipDays;
    }

    public int getAirDays() {
        return airDays;
    }

    public void setAirDays(int airDays) {
        this.airDays = airDays;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }
}