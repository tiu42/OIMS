package com.oims.features.sales_requests.process;

public record SiteStockTransportDTO(
    String siteCode,
    String siteName,
    String country,
    int inStock,
    String unit,
    int shipDays,
    int airDays
) {}
