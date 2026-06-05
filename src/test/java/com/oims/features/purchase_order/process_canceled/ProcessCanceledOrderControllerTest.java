package com.oims.features.purchase_order.process_canceled;

import com.oims.core.dao.IImportSiteDao;
import com.oims.core.dao.IPurchaseOrderDao;
import com.oims.core.dao.IPurchaseOrderItemDao;
import com.oims.core.dao.ISiteMerchandiseDao;
import com.oims.core.dao.ISiteTransportInfoDao;
import com.oims.core.dao.IUserDao;
import com.oims.core.model.ImportSite;
import com.oims.core.model.PurchaseOrder;
import com.oims.core.model.PurchaseOrderItem;
import com.oims.core.model.SiteMerchandise;
import com.oims.core.model.SiteTransportInfo;
import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.features.sales_requests.process.dto.*;
import com.oims.features.sales_requests.process.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessCanceledOrderControllerTest {
    private FakeSiteMerchandiseDao siteMerchandiseDao;
    private ProcessCanceledOrderController controller;

    @BeforeEach
    void setUp() {
        siteMerchandiseDao = new FakeSiteMerchandiseDao();
        controller = new ProcessCanceledOrderController(
                new FakePurchaseOrderDao(),
                new FakePurchaseOrderItemDao(),
                siteMerchandiseDao,
                new FakeSiteTransportInfoDao(),
                new FakeImportSiteDao(),
                new FakeUserDao(),
                new FakePlanGenerationService(),
                new FakePlanPersistenceService()
        );
    }

    @Test
    void getSiteStockAndTransportExcludesCanceledSite() throws SQLException {
        siteMerchandiseDao.stock = List.of(
                new SiteMerchandise("SITE_CANCELLED", "M001", 100, "pcs", LocalDate.now()),
                new SiteMerchandise("SITE01", "M001", 8, "pcs", LocalDate.now())
        );

        List<SiteStockTransportDTO> result = controller.getSiteStockAndTransport("M001", "SITE_CANCELLED");

        assertEquals(1, result.size());
        assertEquals("SITE01", result.get(0).siteCode());
        assertEquals(8, result.get(0).inStock());
        assertEquals(7, result.get(0).shipDays());
        assertEquals(2, result.get(0).airDays());
    }

    @Test
    void getFailedDemandsReturnsDemandWhenRemainingStockIsInsufficient() throws SQLException {
        siteMerchandiseDao.stock = List.of(
                new SiteMerchandise("SITE_CANCELLED", "M001", 100, "pcs", LocalDate.now()),
                new SiteMerchandise("SITE01", "M001", 4, "pcs", LocalDate.now())
        );

        List<ItemDemand> failed = controller.getFailedDemands(
                List.of(new ItemDemand("M001", "Keyboard", 10, "pcs")),
                "SITE_CANCELLED"
        );

        assertEquals(1, failed.size());
        assertEquals("M001", failed.get(0).merchandiseCode());
    }

    @Test
    void getFailedDemandsIgnoresDemandWhenRemainingStockIsEnough() throws SQLException {
        siteMerchandiseDao.stock = List.of(
                new SiteMerchandise("SITE_CANCELLED", "M001", 100, "pcs", LocalDate.now()),
                new SiteMerchandise("SITE01", "M001", 6, "pcs", LocalDate.now()),
                new SiteMerchandise("SITE02", "M001", 5, "pcs", LocalDate.now())
        );

        List<ItemDemand> failed = controller.getFailedDemands(
                List.of(new ItemDemand("M001", "Keyboard", 10, "pcs")),
                "SITE_CANCELLED"
        );

        assertTrue(failed.isEmpty());
    }

    @Test
    void getCreatorNameFallsBackToUserIdWhenUserIsMissing() {
        assertEquals("Người dùng #99", controller.getCreatorName(99));
    }

    private static class FakePurchaseOrderDao implements IPurchaseOrderDao {
        @Override
        public Optional<PurchaseOrder> findById(int orderId) {
            return Optional.empty();
        }

        @Override
        public List<PurchaseOrder> findAll() {
            return List.of();
        }

        @Override
        public List<PurchaseOrder> findByRequestId(int requestId) {
            return List.of();
        }

        @Override
        public List<PurchaseOrder> findByCreatedBy(int createdBy) {
            return List.of();
        }

        @Override
        public int insert(PurchaseOrder purchaseOrder) {
            return 1;
        }

        @Override
        public int insert(Connection connection, PurchaseOrder purchaseOrder) {
            return insert(purchaseOrder);
        }

        @Override
        public boolean update(PurchaseOrder purchaseOrder) {
            return true;
        }

        @Override
        public boolean delete(int orderId) {
            return true;
        }
    }

    private static class FakePurchaseOrderItemDao implements IPurchaseOrderItemDao {
        @Override
        public Optional<PurchaseOrderItem> findById(int orderItemId) {
            return Optional.empty();
        }

        @Override
        public List<PurchaseOrderItem> findAll() {
            return List.of();
        }

        @Override
        public List<PurchaseOrderItem> findByOrderId(int orderId) {
            return List.of();
        }

        @Override
        public int insert(PurchaseOrderItem item) {
            return 1;
        }

        @Override
        public int insert(Connection connection, PurchaseOrderItem item) {
            return insert(item);
        }

        @Override
        public boolean update(PurchaseOrderItem item) {
            return true;
        }

        @Override
        public boolean delete(int orderItemId) {
            return true;
        }
    }

    private static class FakeSiteMerchandiseDao implements ISiteMerchandiseDao {
        private List<SiteMerchandise> stock = List.of();

        @Override
        public Optional<SiteMerchandise> findById(String siteCode, String merchandiseCode) {
            return stock.stream()
                    .filter(item -> item.getSiteCode().equals(siteCode)
                            && item.getMerchandiseCode().equals(merchandiseCode))
                    .findFirst();
        }

        @Override
        public List<SiteMerchandise> findAll() {
            return stock;
        }

        @Override
        public List<SiteMerchandise> findBySiteCode(String siteCode) {
            return stock.stream()
                    .filter(item -> item.getSiteCode().equals(siteCode))
                    .toList();
        }

        @Override
        public List<SiteMerchandise> findByMerchandiseCode(String merchandiseCode) {
            return stock.stream()
                    .filter(item -> item.getMerchandiseCode().equals(merchandiseCode))
                    .toList();
        }

        @Override
        public int insert(SiteMerchandise siteMerchandise) {
            return 1;
        }

        @Override
        public boolean update(SiteMerchandise siteMerchandise) {
            return true;
        }

        @Override
        public boolean delete(String siteCode, String merchandiseCode) {
            return true;
        }
    }

    private static class FakeSiteTransportInfoDao implements ISiteTransportInfoDao {
        @Override
        public Optional<SiteTransportInfo> findById(int transportId) {
            return Optional.empty();
        }

        @Override
        public List<SiteTransportInfo> findAll() {
            return List.of();
        }

        @Override
        public List<SiteTransportInfo> findBySiteCode(String siteCode) {
            if ("SITE01".equals(siteCode)) {
                return List.of(new SiteTransportInfo(1, siteCode, 7, 2, "", LocalDate.now()));
            }
            if ("SITE02".equals(siteCode)) {
                return List.of(new SiteTransportInfo(2, siteCode, 5, 1, "", LocalDate.now()));
            }
            return List.of();
        }

        @Override
        public int insert(SiteTransportInfo siteTransportInfo) {
            return 1;
        }

        @Override
        public boolean update(SiteTransportInfo siteTransportInfo) {
            return true;
        }

        @Override
        public boolean delete(int transportId) {
            return true;
        }
    }

    private static class FakeImportSiteDao implements IImportSiteDao {
        @Override
        public Optional<ImportSite> findById(String siteCode) {
            return Optional.of(new ImportSite(siteCode, siteCode + " Name", "Country", "contact"));
        }

        @Override
        public List<ImportSite> findAll() {
            return List.of();
        }

        @Override
        public int insert(ImportSite importSite) {
            return 1;
        }

        @Override
        public boolean update(ImportSite importSite) {
            return true;
        }

        @Override
        public boolean delete(String siteCode) {
            return true;
        }
    }

    private static class FakeUserDao implements IUserDao {
        @Override
        public Optional<User> findById(int userId) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByCredentials(String username, String password, UserRole role) {
            return Optional.empty();
        }

        @Override
        public List<User> findAll() {
            return List.of();
        }

        @Override
        public int insert(User user) {
            return 1;
        }

        @Override
        public boolean update(User user) {
            return true;
        }

        @Override
        public boolean delete(int userId) {
            return true;
        }
    }

    private static class FakePlanGenerationService implements IPlanGenerationService {
        @Override
        public List<PlanDTO> generatePlans(List<ItemDemand> demands, Map<String, ItemConfig> configs,
                                           Map<String, List<SiteStockTransportDTO>> cachedSiteStock) {
            return List.of();
        }
    }

    private static class FakePlanPersistenceService implements IPlanPersistenceService {
        @Override
        public void savePlan(int requestId, int creatorUserId, PlanDTO plan, boolean hasErrors) {
        }
    }
}
