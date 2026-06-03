package com.oims.features.sales_requests.process;

public record ProcessedErrorDTO(
    String merchandiseCode,
    String merchandiseName,
    int requestedQty,
    int allocatedQty,
    int missingQty,
    String unit,
    String reason
) {}
