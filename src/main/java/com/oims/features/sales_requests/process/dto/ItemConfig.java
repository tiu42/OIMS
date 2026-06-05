package com.oims.features.sales_requests.process.dto;

import com.oims.core.model.DeliveryMeans;

public record ItemConfig(
    String preferredSite,
    String nonPreferredSite,
    DeliveryMeans preferredDelivery
) {}
