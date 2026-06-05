package com.oims.features.sales_requests.process.controller;

import com.oims.features.sales_requests.process.dto.*;
import com.oims.features.sales_requests.process.service.*;

import java.sql.SQLException;
import java.util.*;

public class ProcessResultController {
    private final IProcessResultService processResultService;

    public ProcessResultController() {
        this.processResultService = new ProcessResultService();
    }

    public ProcessResultController(IProcessResultService processResultService) {
        this.processResultService = processResultService;
    }

    public Optional<SalesRequestDTO> getSalesRequest(int requestId) throws SQLException {
        return processResultService.getSalesRequest(requestId);
    }

    public String getCreatorName(int userId) {
        return processResultService.getCreatorName(userId);
    }

    public List<PurchaseOrderDTO> getPurchaseOrders(int requestId) throws SQLException {
        return processResultService.getPurchaseOrders(requestId);
    }

    public List<AllocatedItem> getPurchaseOrderItems(int orderId) throws SQLException {
        return processResultService.getPurchaseOrderItems(orderId);
    }

    public String resolveMerchandiseName(String merchandiseCode) {
        return processResultService.resolveMerchandiseName(merchandiseCode);
    }

    public String resolveSiteName(String siteCode) {
        return processResultService.resolveSiteName(siteCode);
    }

    public List<ProcessedErrorDTO> getProcessingErrors(int requestId) throws SQLException {
        return processResultService.getProcessingErrors(requestId);
    }
}
