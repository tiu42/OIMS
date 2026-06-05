package com.oims.features.sales_requests.process.dto;

import com.oims.core.model.DeliveryMeans;
import java.util.List;

public record AllocatedOrder(
    String siteCode,
    String siteName,
    DeliveryMeans deliveryMeans,
    List<AllocatedItem> items
) {}
