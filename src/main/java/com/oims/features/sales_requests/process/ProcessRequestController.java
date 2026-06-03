package com.oims.features.sales_requests.process;

import com.oims.core.dao.*;
import com.oims.core.model.*;

import java.sql.SQLException;
import java.util.*;

public class ProcessRequestController {
    private final ISalesRequestDao salesRequestDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final ISiteMerchandiseDao siteMerchandiseDao;
    private final ISiteTransportInfoDao siteTransportInfoDao;
    private final IImportSiteDao importSiteDao;
    private final IUserDao userDao;

    private final IPlanGenerationService planGenerationService;
    private final IPlanPersistenceService planPersistenceService;

    public ProcessRequestController() {
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
        this.siteMerchandiseDao = DaoFactory.getSiteMerchandiseDao();
        this.siteTransportInfoDao = DaoFactory.getSiteTransportInfoDao();
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.userDao = DaoFactory.getUserDao();
        this.planGenerationService = ServiceFactory.getPlanGenerationService();
        this.planPersistenceService = ServiceFactory.getPlanPersistenceService();
    }

    public ProcessRequestController(ISalesRequestDao salesRequestDao, ISalesRequestItemDao salesRequestItemDao,
                                    ISiteMerchandiseDao siteMerchandiseDao, ISiteTransportInfoDao siteTransportInfoDao,
                                    IImportSiteDao importSiteDao, IUserDao userDao,
                                    IPlanGenerationService planGenerationService, IPlanPersistenceService planPersistenceService) {
        this.salesRequestDao = salesRequestDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.siteMerchandiseDao = siteMerchandiseDao;
        this.siteTransportInfoDao = siteTransportInfoDao;
        this.importSiteDao = importSiteDao;
        this.userDao = userDao;
        this.planGenerationService = planGenerationService;
        this.planPersistenceService = planPersistenceService;
    }

    public Optional<SalesRequest> getSalesRequest(int requestId) throws SQLException {
        return salesRequestDao.findById(requestId);
    }

    public List<SalesRequestItem> getSalesRequestItems(int requestId) throws SQLException {
        return salesRequestItemDao.findByRequestId(requestId);
    }

    public String getCreatorName(int userId) {
        try {
            return userDao.findById(userId)
                    .map(user -> {
                        String fullName = user.getFullName();
                        if (fullName != null && !fullName.isBlank()) return fullName;
                        return user.getUsername() == null || user.getUsername().isBlank() ? "Không xác định" : user.getUsername();
                    })
                    .orElse("Người dùng #" + userId);
        } catch (SQLException e) {
            return "Người dùng #" + userId;
        }
    }

    public List<SiteStockTransportDTO> getSiteStockAndTransport(String merchandiseCode) throws SQLException {
        List<SiteStockTransportDTO> result = new ArrayList<>();
        List<SiteMerchandise> siteMerches = siteMerchandiseDao.findByMerchandiseCode(merchandiseCode);
        
        for (SiteMerchandise sm : siteMerches) {
            String siteCode = sm.getSiteCode();
            String siteName = "Không xác định";
            String country = "Không xác định";
            Optional<ImportSite> siteOpt = importSiteDao.findById(siteCode);
            if (siteOpt.isPresent()) {
                siteName = siteOpt.get().getSiteName();
                country = siteOpt.get().getCountry();
            }

            int shipDays = 0;
            int airDays = 0;
            List<SiteTransportInfo> transInfos = siteTransportInfoDao.findBySiteCode(siteCode);
            if (!transInfos.isEmpty()) {
                shipDays = transInfos.get(0).getShipDays();
                airDays = transInfos.get(0).getAirDays();
            }

            result.add(new SiteStockTransportDTO(
                    siteCode,
                    siteName,
                    country,
                    sm.getInStockQuantity(),
                    sm.getUnit(),
                    shipDays,
                    airDays
            ));
        }
        return result;
    }

    // Checking which items cannot be satisfied due to lack of stock or missing suppliers
    public List<ItemDemand> getFailedDemands(List<ItemDemand> demands) throws SQLException {
        List<ItemDemand> failed = new ArrayList<>();
        for (ItemDemand demand : demands) {
            List<SiteStockTransportDTO> sites = getSiteStockAndTransport(demand.merchandiseCode());
            int totalStock = sites.stream().mapToInt(SiteStockTransportDTO::inStock).sum();
            if (sites.isEmpty() || totalStock < demand.quantity()) {
                failed.add(demand);
            }
        }
        return failed;
    }

    public List<PlanDTO> generatePlans(
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs,
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock) throws SQLException {
        return planGenerationService.generatePlans(demands, configs, cachedSiteStock);
    }

    public void savePlan(int requestId, User creator, PlanDTO plan, boolean hasErrors) throws SQLException {
        planPersistenceService.savePlan(requestId, creator, plan, hasErrors);
    }
}
