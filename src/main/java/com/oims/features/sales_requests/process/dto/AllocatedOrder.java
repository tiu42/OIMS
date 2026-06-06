package com.oims.features.sales_requests.process.dto;

import com.oims.core.model.DeliveryMeans;
import java.time.LocalDate;
import java.util.List;

public record AllocatedOrder(
    String siteCode,
    String siteName,
    DeliveryMeans deliveryMeans,
    List<AllocatedItem> items,
    LocalDate expectedDeliveryDate
) {
    public AllocatedOrder(String siteCode, String siteName, DeliveryMeans deliveryMeans, List<AllocatedItem> items) {
        this(siteCode, siteName, deliveryMeans, items, null);
    }
}
