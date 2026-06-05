package com.oims.features.sales_requests.process.controller;

import com.oims.features.sales_requests.process.dto.*;
import com.oims.features.sales_requests.process.service.*;

import java.sql.SQLException;
import java.util.*;

public class ProcessRequestController {
    private final IProcessRequestService processRequestService;
    private final ISiteStockService siteStockService;
    private final IPlanGenerationService planGenerationService;
    private final IPlanPersistenceService planPersistenceService;

    public ProcessRequestController() {
        this.processRequestService = ServiceFactory.getProcessRequestService();
        this.siteStockService = ServiceFactory.getSiteStockService();
        this.planGenerationService = ServiceFactory.getPlanGenerationService();
        this.planPersistenceService = ServiceFactory.getPlanPersistenceService();
    }

    public ProcessRequestController(IProcessRequestService processRequestService,
                                    ISiteStockService siteStockService,
                                    IPlanGenerationService planGenerationService,
                                    IPlanPersistenceService planPersistenceService) {
        this.processRequestService = processRequestService;
        this.siteStockService = siteStockService;
        this.planGenerationService = planGenerationService;
        this.planPersistenceService = planPersistenceService;
    }

    public Optional<SalesRequestDTO> getSalesRequest(int requestId) throws SQLException {
        return processRequestService.getSalesRequest(requestId);
    }

    public List<ItemDemand> getDemands(int requestId) throws SQLException {
        return processRequestService.getDemands(requestId);
    }

    public String getCreatorName(int userId) {
        return processRequestService.getCreatorName(userId);
    }

    public List<SiteStockTransportDTO> getSiteStockAndTransport(String merchandiseCode) throws SQLException {
        return siteStockService.getSiteStockAndTransport(merchandiseCode, null);
    }

    public List<ItemDemand> getFailedDemands(List<ItemDemand> demands) throws SQLException {
        return siteStockService.getFailedDemands(demands, null);
    }

    public List<PlanDTO> generatePlans(
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs,
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock) throws SQLException {
        return planGenerationService.generatePlans(demands, configs, cachedSiteStock);
    }

    public void savePlan(int requestId, int creatorUserId, PlanDTO plan, boolean hasErrors) throws SQLException {
        planPersistenceService.savePlan(requestId, creatorUserId, plan, hasErrors);
    }

    public boolean beginProcessing(int requestId) throws SQLException {
        return processRequestService.beginProcessing(requestId);
    }

    public void cancelProcessing(int requestId) throws SQLException {
        processRequestService.cancelProcessing(requestId);
    }
}
