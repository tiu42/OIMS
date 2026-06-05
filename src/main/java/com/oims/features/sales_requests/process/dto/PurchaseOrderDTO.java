package com.oims.features.sales_requests.process.dto;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.PurchaseOrderStatus;
import java.time.LocalDate;

public record PurchaseOrderDTO(
    int orderId,
    int requestId,
    String siteCode,
    String siteName,
    int createdBy,
    LocalDate orderDate,
    DeliveryMeans deliveryMeans,
    PurchaseOrderStatus status
) {}
