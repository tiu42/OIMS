package com.oims.core.model;

import java.time.LocalDate;

public class SalesRequest {
    private Integer requestId;
    private Integer createdBy;
    private LocalDate createdDate;
    private SalesRequestStatus status;

    public SalesRequest() {
    }

    public SalesRequest(Integer requestId, Integer createdBy, LocalDate createdDate, SalesRequestStatus status) {
        this.requestId = requestId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.status = status;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public SalesRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SalesRequestStatus status) {
        this.status = status;
    }
}