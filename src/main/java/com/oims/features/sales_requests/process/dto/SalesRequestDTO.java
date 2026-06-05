package com.oims.features.sales_requests.process.dto;

import java.time.LocalDate;

public record SalesRequestDTO(
    int requestId,
    int createdBy,
    LocalDate createdDate,
    String statusText
) {}
