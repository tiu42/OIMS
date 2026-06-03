package com.oims.features.sales_requests.process;

public record ItemDemand(
    String merchandiseCode,
    String merchandiseName,
    int quantity,
    String unit
) {}
