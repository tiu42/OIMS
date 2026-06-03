package com.oims.features.sales_requests.list;

import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record RequestListRow(SalesRequest salesRequest, String creatorName) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Integer getRequestId() {
        return salesRequest == null ? null : salesRequest.getRequestId();
    }

    public Integer getCreatedBy() {
        return salesRequest == null ? null : salesRequest.getCreatedBy();
    }

    public LocalDate getCreatedDate() {
        return salesRequest == null ? null : salesRequest.getCreatedDate();
    }

    public String getCreatedDateDisplay() {
        LocalDate createdDate = getCreatedDate();
        return createdDate == null ? "" : createdDate.format(DATE_FORMATTER);
    }

    public SalesRequestStatus getStatus() {
        return salesRequest == null ? null : salesRequest.getStatus();
    }

    public String getStatusLabel() {
        SalesRequestStatus status = getStatus();
        if (status == null) {
            return "Không xác định";
        }

        return switch (status) {
            case PENDING -> "Chờ xử lý";
            case PROCESSING -> "Đang xử lý";
            case COMPLETED -> "Hoàn tất";
            case ERROR -> "Lỗi";
        };
    }

    public String getRequestIdDisplay() {
        Integer requestId = getRequestId();
        return requestId == null ? "" : String.valueOf(requestId);
    }

    public String getSearchText() {
        return String.join(" ",
                safeText(getRequestIdDisplay()),
                safeText(creatorName()),
                safeText(getCreatedDateDisplay()),
                safeText(getStatusLabel())
        ).toLowerCase();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
