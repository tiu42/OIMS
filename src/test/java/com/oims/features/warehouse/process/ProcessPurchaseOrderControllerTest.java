package com.oims.features.warehouse.process;

import com.oims.core.dao.IImportSiteDao;
import com.oims.core.dao.IMerchandiseDao;
import com.oims.core.dao.IPurchaseOrderDao;
import com.oims.core.dao.IPurchaseOrderItemDao;
import com.oims.core.dao.IUserDao;
import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.ImportSite;
import com.oims.core.model.Merchandise;
import com.oims.core.model.PurchaseOrder;
import com.oims.core.model.PurchaseOrderItem;
import com.oims.core.model.PurchaseOrderStatus;
import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessPurchaseOrderControllerTest {
    private FakePurchaseOrderDao purchaseOrderDao;
    private ProcessPurchaseOrderController controller;

    @BeforeEach
    void setUp() {
        purchaseOrderDao = new FakePurchaseOrderDao();
        controller = new ProcessPurchaseOrderController(
                purchaseOrderDao,
                new FakePurchaseOrderItemDao(),
                new FakeUserDao(),
                new FakeImportSiteDao(),
                new FakeMerchandiseDao()
        );
    }

    @Test
    void approveReceiptWithSentOrderAndFullItemsMarksOrderDelivered() throws SQLException {
        PurchaseOrder order = createOrder(1, PurchaseOrderStatus.SENT);
        purchaseOrderDao.save(order);

        controller.validateAndApproveReceipt(1, List.of(
                new ProcessPurchaseOrderController.ItemShortageResult("M001", "Keyboard", "Đủ", 10, 0),
                new ProcessPurchaseOrderController.ItemShortageResult("M002", "Mouse", "Đủ", 5, 0)
        ));

        assertEquals(PurchaseOrderStatus.DELIVERED, order.getStatus());
        assertTrue(purchaseOrderDao.updated);
    }

    @Test
    void approveReceiptWithConfirmedOrderAndValidShortageMarksOrderDelivered() throws SQLException {
        PurchaseOrder order = createOrder(2, PurchaseOrderStatus.CONFIRMED);
        purchaseOrderDao.save(order);

        controller.validateAndApproveReceipt(2, List.of(
                new ProcessPurchaseOrderController.ItemShortageResult("M003", "Monitor", "Thiếu", 10, 3)
        ));

        assertEquals(PurchaseOrderStatus.DELIVERED, order.getStatus());
        assertTrue(purchaseOrderDao.updated);
    }

    @Test
    void approveReceiptRejectsShortageEqualToZero() {
        PurchaseOrder order = createOrder(3, PurchaseOrderStatus.SENT);
        purchaseOrderDao.save(order);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.validateAndApproveReceipt(3, List.of(
                        new ProcessPurchaseOrderController.ItemShortageResult("M004", "Laptop", "Thiếu", 10, 0)
                ))
        );

        assertEquals("Số lượng thiếu phải lớn hơn 0 cho mặt hàng: Laptop", exception.getMessage());
        assertEquals(PurchaseOrderStatus.SENT, order.getStatus());
        assertFalse(purchaseOrderDao.updated);
    }

    @Test
    void approveReceiptRejectsShortageGreaterThanOrderedQuantity() {
        PurchaseOrder order = createOrder(4, PurchaseOrderStatus.SENT);
        purchaseOrderDao.save(order);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.validateAndApproveReceipt(4, List.of(
                        new ProcessPurchaseOrderController.ItemShortageResult("M005", "Printer", "Thiếu", 10, 11)
                ))
        );

        assertEquals("Số lượng thiếu (11) không được vượt quá số lượng đặt (10) cho mặt hàng: Printer", exception.getMessage());
        assertEquals(PurchaseOrderStatus.SENT, order.getStatus());
        assertFalse(purchaseOrderDao.updated);
    }

    @Test
    void approveReceiptRejectsOrderInInvalidStatus() {
        PurchaseOrder order = createOrder(5, PurchaseOrderStatus.DELIVERED);
        purchaseOrderDao.save(order);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.validateAndApproveReceipt(5, List.of(
                        new ProcessPurchaseOrderController.ItemShortageResult("M006", "Cable", "Đủ", 10, 0)
                ))
        );

        assertEquals("Chỉ có thể duyệt đơn hàng nhập kho ở trạng thái chưa xác nhận.", exception.getMessage());
        assertEquals(PurchaseOrderStatus.DELIVERED, order.getStatus());
        assertFalse(purchaseOrderDao.updated);
    }

    @Test
    void approveReceiptRejectsMissingOrder() {
        SQLException exception = assertThrows(SQLException.class, () ->
                controller.validateAndApproveReceipt(999, List.of(
                        new ProcessPurchaseOrderController.ItemShortageResult("M007", "Adapter", "Đủ", 10, 0)
                ))
        );

        assertEquals("Đơn hàng không tồn tại để duyệt.", exception.getMessage());
        assertFalse(purchaseOrderDao.updated);
    }

    private PurchaseOrder createOrder(int orderId, PurchaseOrderStatus status) {
        return new PurchaseOrder(
                orderId,
                100,
                "SITE01",
                200,
                LocalDate.of(2026, 6, 4),
                DeliveryMeans.SHIP_DELIVERY,
                status
        );
    }

    private static class FakePurchaseOrderDao implements IPurchaseOrderDao {
        private final Map<Integer, PurchaseOrder> orders = new HashMap<>();
        private boolean updated;

        void save(PurchaseOrder order) {
            orders.put(order.getOrderId(), order);
        }

        @Override
        public Optional<PurchaseOrder> findById(int orderId) {
            return Optional.ofNullable(orders.get(orderId));
        }

        @Override
        public List<PurchaseOrder> findAll() {
            return new ArrayList<>(orders.values());
        }

        @Override
        public List<PurchaseOrder> findByRequestId(int requestId) {
            return orders.values().stream()
                    .filter(order -> order.getRequestId() == requestId)
                    .toList();
        }

        @Override
        public List<PurchaseOrder> findByCreatedBy(int createdBy) {
            return orders.values().stream()
                    .filter(order -> order.getCreatedBy() == createdBy)
                    .toList();
        }

        @Override
        public int insert(PurchaseOrder purchaseOrder) {
            int id = purchaseOrder.getOrderId() == null ? orders.size() + 1 : purchaseOrder.getOrderId();
            purchaseOrder.setOrderId(id);
            orders.put(id, purchaseOrder);
            return id;
        }

        @Override
        public int insert(Connection connection, PurchaseOrder purchaseOrder) {
            return insert(purchaseOrder);
        }

        @Override
        public boolean update(PurchaseOrder purchaseOrder) {
            updated = true;
            orders.put(purchaseOrder.getOrderId(), purchaseOrder);
            return true;
        }

        @Override
        public boolean delete(int orderId) {
            return orders.remove(orderId) != null;
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

    private static class FakeImportSiteDao implements IImportSiteDao {
        @Override
        public Optional<ImportSite> findById(String siteCode) {
            return Optional.empty();
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

    private static class FakeMerchandiseDao implements IMerchandiseDao {
        @Override
        public Optional<Merchandise> findById(String merchandiseCode) {
            return Optional.empty();
        }

        @Override
        public List<Merchandise> findAll() {
            return List.of();
        }

        @Override
        public int insert(Merchandise merchandise) {
            return 1;
        }

        @Override
        public boolean update(Merchandise merchandise) {
            return true;
        }

        @Override
        public boolean delete(String merchandiseCode) {
            return true;
        }
    }
}
