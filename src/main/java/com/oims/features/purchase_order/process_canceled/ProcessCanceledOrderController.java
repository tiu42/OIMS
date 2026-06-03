package com.oims.features.purchase_order.process_canceled;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.features.sales_requests.process.*;

import java.sql.SQLException;
import java.util.*;

public class ProcessCanceledOrderController {
    private final IPurchaseOrderDao purchaseOrderDao;
    private final IPurchaseOrderItemDao purchaseOrderItemDao;
    private final ISiteMerchandiseDao siteMerchandiseDao;
    private final ISiteTransportInfoDao siteTransportInfoDao;
    private final IImportSiteDao importSiteDao;
    private final IUserDao userDao;

    private final IPlanGenerationService planGenerationService;
    private final IPlanPersistenceService planPersistenceService;

    public ProcessCanceledOrderController() {
        this.purchaseOrderDao = DaoFactory.getPurchaseOrderDao();
        this.purchaseOrderItemDao = DaoFactory.getPurchaseOrderItemDao();
        this.siteMerchandiseDao = DaoFactory.getSiteMerchandiseDao();
        this.siteTransportInfoDao = DaoFactory.getSiteTransportInfoDao();
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.userDao = DaoFactory.getUserDao();
        this.planGenerationService = ServiceFactory.getPlanGenerationService();
        this.planPersistenceService = ServiceFactory.getPlanPersistenceService();
    }

    public ProcessCanceledOrderController(IPurchaseOrderDao purchaseOrderDao, IPurchaseOrderItemDao purchaseOrderItemDao,
                                          ISiteMerchandiseDao siteMerchandiseDao, ISiteTransportInfoDao siteTransportInfoDao,
                                          IImportSiteDao importSiteDao, IUserDao userDao,
                                          IPlanGenerationService planGenerationService, IPlanPersistenceService planPersistenceService) {
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.siteMerchandiseDao = siteMerchandiseDao;
        this.siteTransportInfoDao = siteTransportInfoDao;
        this.importSiteDao = importSiteDao;
        this.userDao = userDao;
        this.planGenerationService = planGenerationService;
        this.planPersistenceService = planPersistenceService;
    }

    public Optional<PurchaseOrder> getPurchaseOrder(int orderId) throws SQLException {
        return purchaseOrderDao.findById(orderId);
    }

    public List<PurchaseOrderItem> getPurchaseOrderItems(int orderId) throws SQLException {
        return purchaseOrderItemDao.findByOrderId(orderId);
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

    public List<SiteStockTransportDTO> getSiteStockAndTransport(String merchandiseCode, String excludedSiteCode) throws SQLException {
        List<SiteStockTransportDTO> result = new ArrayList<>();
        List<SiteMerchandise> siteMerches = siteMerchandiseDao.findByMerchandiseCode(merchandiseCode);
        
        for (SiteMerchandise sm : siteMerches) {
            String siteCode = sm.getSiteCode();
            if (siteCode.equals(excludedSiteCode)) {
                continue; // Skip the excluded site (site of the canceled purchase order)
            }
            
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

    public List<ItemDemand> getFailedDemands(List<ItemDemand> demands, String excludedSiteCode) throws SQLException {
        List<ItemDemand> failed = new ArrayList<>();
        for (ItemDemand demand : demands) {
            List<SiteStockTransportDTO> sites = getSiteStockAndTransport(demand.merchandiseCode(), excludedSiteCode);
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
