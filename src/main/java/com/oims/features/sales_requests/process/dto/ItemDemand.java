package com.oims.features.sales_requests.process.dto;

import java.time.LocalDate;

public record ItemDemand(
    String merchandiseCode,
    String merchandiseName,
    int quantity,
    String unit,
    LocalDate desiredDeliveryDate
) {
    public ItemDemand(String merchandiseCode, String merchandiseName, int quantity, String unit) {
        this(merchandiseCode, merchandiseName, quantity, unit, null);
    }
}
