package com.oims.features.purchase_order.process_canceled;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.features.sales_requests.process.dto.*;
import com.oims.features.sales_requests.process.service.*;

import java.sql.SQLException;
import java.util.*;

public class ProcessCanceledOrderController {
    private final IPurchaseOrderDao purchaseOrderDao;
    private final IPurchaseOrderItemDao purchaseOrderItemDao;
    private final IUserDao userDao;

    private final ISiteStockService siteStockService;
    private final IPlanGenerationService planGenerationService;
    private final IPlanPersistenceService planPersistenceService;

    public ProcessCanceledOrderController() {
        this.purchaseOrderDao = DaoFactory.getPurchaseOrderDao();
        this.purchaseOrderItemDao = DaoFactory.getPurchaseOrderItemDao();
        this.userDao = DaoFactory.getUserDao();
        this.siteStockService = ServiceFactory.getSiteStockService();
        this.planGenerationService = ServiceFactory.getPlanGenerationService();
        this.planPersistenceService = ServiceFactory.getPlanPersistenceService();
    }

    public ProcessCanceledOrderController(IPurchaseOrderDao purchaseOrderDao, IPurchaseOrderItemDao purchaseOrderItemDao,
                                          ISiteMerchandiseDao siteMerchandiseDao, ISiteTransportInfoDao siteTransportInfoDao,
                                          IImportSiteDao importSiteDao, IUserDao userDao,
                                          IPlanGenerationService planGenerationService, IPlanPersistenceService planPersistenceService) {
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.userDao = userDao;
        this.siteStockService = new SiteStockService(siteMerchandiseDao, siteTransportInfoDao, importSiteDao);
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
        return siteStockService.getSiteStockAndTransport(merchandiseCode, excludedSiteCode);
    }

    public List<ItemDemand> getFailedDemands(List<ItemDemand> demands, String excludedSiteCode) throws SQLException {
        return siteStockService.getFailedDemands(demands, excludedSiteCode);
    }

    public List<PlanDTO> generatePlans(
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs,
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock) throws SQLException {
        return planGenerationService.generatePlans(demands, configs, cachedSiteStock);
    }

    public void savePlan(int requestId, User creator, PlanDTO plan, boolean hasErrors) throws SQLException {
        planPersistenceService.savePlan(requestId, creator.getUserId(), plan, hasErrors);
    }
}
