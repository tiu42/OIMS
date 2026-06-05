package com.oims.features.sales_requests.dto;

import java.time.LocalDate;

public record RequestItemDTO(String merchandiseCode, int quantity, String unit, LocalDate desiredDate) {
}
