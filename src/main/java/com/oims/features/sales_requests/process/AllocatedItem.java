package com.oims.features.sales_requests.process;

public record AllocatedItem(
    String merchandiseCode,
    String merchandiseName,
    int quantity,
    String unit
) {}
