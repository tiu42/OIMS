package com.oims.features.sales_requests.process.dto;

import com.oims.core.model.DeliveryMeans;

public record AllocatedItemAllocation(
    String siteCode,
    String merchandiseCode,
    String merchandiseName,
    int quantity,
    String unit,
    DeliveryMeans deliveryMeans
) {}
