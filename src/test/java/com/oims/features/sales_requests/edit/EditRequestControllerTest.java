package com.oims.features.sales_requests.edit;

import com.oims.core.dao.IMerchandiseDao;
import com.oims.core.dao.ISalesRequestDao;
import com.oims.core.dao.ISalesRequestItemDao;
import com.oims.core.dao.IUserDao;
import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import com.oims.features.sales_requests.dto.RequestItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditRequestControllerTest {
    private FakeSalesRequestDao salesRequestDao;
    private FakeSalesRequestItemDao salesRequestItemDao;
    private EditRequestController controller;
    private static List<java.sql.Driver> originalDrivers = new ArrayList<>();
    private static MockDriver mockDriver;

    @BeforeEach
    void setUp() throws SQLException {
        salesRequestDao = new FakeSalesRequestDao();
        salesRequestItemDao = new FakeSalesRequestItemDao();

        IEditRequestService editRequestService = new EditRequestService(
                new FakeMerchandiseDao(),
                salesRequestDao,
                salesRequestItemDao,
                new FakeUserDao(),
                new DefaultSalesRequestEditPolicy()
        );

        controller = new EditRequestController(
                editRequestService,
                new DefaultSalesRequestEditPolicy()
        );

        Connection mockConnection = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> null
        );

        java.util.Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
        originalDrivers.clear();
        while (drivers.hasMoreElements()) {
            java.sql.Driver d = drivers.nextElement();
            originalDrivers.add(d);
            java.sql.DriverManager.deregisterDriver(d);
        }

        mockDriver = new MockDriver(mockConnection);
        java.sql.DriverManager.registerDriver(mockDriver);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws SQLException {
        if (mockDriver != null) {
            java.sql.DriverManager.deregisterDriver(mockDriver);
        }
        for (java.sql.Driver d : originalDrivers) {
            java.sql.DriverManager.registerDriver(d);
        }
    }

    @Test
    void updateSalesRequestWithPendingRequestReplacesExistingItems() throws Exception {
        salesRequestDao.request = createRequest(1, SalesRequestStatus.PENDING);
        salesRequestItemDao.items.add(createItem(101, 1, "OLD", 1));

        controller.updateSalesRequest(1, createUser(10), List.of(
                new RequestItemDTO("M001", 5, "pcs", LocalDate.now().plusDays(3)),
                new RequestItemDTO("M002", 8, "box", LocalDate.now().plusDays(4))
        ));

        assertTrue(salesRequestItemDao.deleteByRequestIdCalled);
        assertEquals(2, salesRequestItemDao.items.size());
        assertEquals("M001", salesRequestItemDao.items.get(0).getMerchandiseCode());
        assertEquals("M002", salesRequestItemDao.items.get(1).getMerchandiseCode());
    }

    @Test
    void updateSalesRequestRejectsMissingModifier() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.updateSalesRequest(1, null, List.of(
                        new RequestItemDTO("M001", 5, "pcs", LocalDate.now().plusDays(3))
                ))
        );

        assertEquals("Không tìm thấy thông tin người chỉnh sửa. Vui lòng đăng nhập lại.", exception.getMessage());
    }

    @Test
    void updateSalesRequestRejectsMissingRequest() {
        salesRequestDao.request = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.updateSalesRequest(404, createUser(10), List.of(
                        new RequestItemDTO("M001", 5, "pcs", LocalDate.now().plusDays(3))
                ))
        );

        assertEquals("Yêu cầu nhập hàng không tồn tại.", exception.getMessage());
    }

    @Test
    void updateSalesRequestRejectsNonPendingRequest() {
        salesRequestDao.request = createRequest(1, SalesRequestStatus.COMPLETED);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.updateSalesRequest(1, createUser(10), List.of(
                        new RequestItemDTO("M001", 5, "pcs", LocalDate.now().plusDays(3))
                ))
        );

        assertEquals("Yêu cầu đã được xử lý xong. Không thể chỉnh sửa.", exception.getMessage());
    }

    @Test
    void updateSalesRequestRejectsPastDesiredDate() {
        salesRequestDao.request = createRequest(1, SalesRequestStatus.PENDING);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.updateSalesRequest(1, createUser(10), List.of(
                        new RequestItemDTO("M001", 5, "pcs", LocalDate.now().minusDays(1))
                ))
        );

        assertEquals("Ngày nhận mong muốn không được ở trong quá khứ.", exception.getMessage());
    }

    private SalesRequest createRequest(int requestId, SalesRequestStatus status) {
        return new SalesRequest(requestId, 10, LocalDate.now(), status);
    }

    private SalesRequestItem createItem(int itemId, int requestId, String code, int quantity) {
        return new SalesRequestItem(itemId, requestId, code, quantity, "pcs", LocalDate.now().plusDays(1));
    }

    private User createUser(int userId) {
        return new User(userId, "sales", "pw", "Sales User", "sales@example.com",
                UserRole.SALES, LocalDate.now(), true);
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

    private static class FakeSalesRequestDao implements ISalesRequestDao {
        private SalesRequest request;

        @Override
        public Optional<SalesRequest> findById(int requestId) {
            return request != null && request.getRequestId() == requestId
                    ? Optional.of(request)
                    : Optional.empty();
        }

        @Override
        public List<SalesRequest> findAll() {
            return request == null ? List.of() : List.of(request);
        }

        @Override
        public List<SalesRequest> findByCreatedBy(int createdBy) {
            return findAll().stream()
                    .filter(item -> item.getCreatedBy() == createdBy)
                    .toList();
        }

        @Override
        public int insert(SalesRequest salesRequest) {
            request = salesRequest;
            return 1;
        }

        @Override
        public boolean update(SalesRequest salesRequest) {
            request = salesRequest;
            return true;
        }

        @Override
        public boolean delete(int requestId) {
            request = null;
            return true;
        }

        @Override
        public boolean updateStatus(Connection connection, int requestId, SalesRequestStatus status) {
            if (request == null || request.getRequestId() != requestId) {
                return false;
            }
            request.setStatus(status);
            return true;
        }

        @Override
        public boolean updateStatus(int requestId, SalesRequestStatus status) {
            return true;
        }

        @Override
        public boolean updateStatusIfCurrent(int requestId, SalesRequestStatus expectedStatus, SalesRequestStatus newStatus) {
            return true;
        }
    }

    private static class FakeSalesRequestItemDao implements ISalesRequestItemDao {
        private final List<SalesRequestItem> items = new ArrayList<>();
        private boolean deleteByRequestIdCalled;

        @Override
        public Optional<SalesRequestItem> findById(int itemId) {
            return items.stream()
                    .filter(item -> item.getItemId() == itemId)
                    .findFirst();
        }

        @Override
        public List<SalesRequestItem> findAll() {
            return items;
        }

        @Override
        public List<SalesRequestItem> findByRequestId(int requestId) {
            return items.stream()
                    .filter(item -> item.getRequestId() == requestId)
                    .toList();
        }

        @Override
        public int insert(SalesRequestItem item) {
            items.add(item);
            return items.size();
        }

        @Override
        public int insert(Connection connection, SalesRequestItem item) {
            return insert(item);
        }

        @Override
        public boolean update(SalesRequestItem item) {
            return true;
        }

        @Override
        public boolean delete(int itemId) {
            return items.removeIf(item -> item.getItemId() == itemId);
        }

        @Override
        public void deleteByRequestId(int requestId) {
            deleteByRequestIdCalled = true;
            items.removeIf(item -> item.getRequestId() == requestId);
        }

        @Override
        public void deleteByRequestId(Connection connection, int requestId) {
            deleteByRequestId(requestId);
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

    private static class MockDriver implements java.sql.Driver {
        private final Connection mockConnection;

        public MockDriver(Connection mockConnection) {
            this.mockConnection = mockConnection;
        }

        @Override
        public Connection connect(String url, java.util.Properties info) throws SQLException {
            if (url.startsWith("jdbc:mysql://localhost:3306/glocerimex")) {
                return mockConnection;
            }
            return null;
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return url.startsWith("jdbc:mysql://localhost:3306/glocerimex");
        }

        @Override
        public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) throws SQLException {
            return new java.sql.DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return true;
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
            throw new java.sql.SQLFeatureNotSupportedException();
        }
    }
}
