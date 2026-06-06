package com.oims.features.site.detail;

import com.oims.core.model.SiteTransportInfo;

import java.time.format.DateTimeFormatter;

public final class SiteTransportRow {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int shipDays;
    private final int airDays;
    private final String otherInfo;
    private final String updatedDate;

    public SiteTransportRow(SiteTransportInfo info) {
        this.shipDays = info.getShipDays();
        this.airDays = info.getAirDays();
        this.otherInfo = info.getOtherInfo() == null ? "" : info.getOtherInfo();
        this.updatedDate = info.getUpdatedDate() == null ? "" : info.getUpdatedDate().format(DATE_FORMAT);
    }

    public String getShipDays() {
        return String.valueOf(shipDays);
    }

    public String getAirDays() {
        return String.valueOf(airDays);
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }
}
