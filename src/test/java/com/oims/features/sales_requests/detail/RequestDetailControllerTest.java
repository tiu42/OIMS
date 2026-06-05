package com.oims.features.sales_requests.detail;

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
import com.oims.core.session.AppSession;
import com.oims.features.sales_requests.detail.RequestDetailController.RequestDetailDTO;
import com.oims.features.sales_requests.edit.DefaultSalesRequestEditPolicy;
import com.oims.features.sales_requests.edit.EditRequestService;
import com.oims.features.sales_requests.edit.IEditRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestDetailControllerTest {
    private FakeSalesRequestDao salesRequestDao;
    private FakeSalesRequestItemDao salesRequestItemDao;
    private FakeMerchandiseDao merchandiseDao;
    private RequestDetailController controller;

    @BeforeEach
    void setUp() {
        salesRequestDao = new FakeSalesRequestDao();
        salesRequestItemDao = new FakeSalesRequestItemDao();
        merchandiseDao = new FakeMerchandiseDao();

        IEditRequestService editRequestService = new EditRequestService(
                merchandiseDao,
                salesRequestDao,
                salesRequestItemDao,
                new FakeUserDao(),
                new DefaultSalesRequestEditPolicy()
        );

        controller = new RequestDetailController(
                salesRequestDao,
                new FakeUserDao(),
                salesRequestItemDao,
                merchandiseDao,
                editRequestService,
                new DefaultSalesRequestEditPolicy()
        );
        AppSession.getInstance().setSelectedRequestId(7);
    }

    @Test
    void loadRequestDataReturnsFormattedDetailInformation() throws Exception {
        salesRequestDao.request = new SalesRequest(7, 10, LocalDate.of(2026, 6, 4), SalesRequestStatus.PENDING);

        RequestDetailDTO detail = controller.loadRequestData();

        assertEquals("7", detail.id());
        assertEquals("Nguyen Van Sales", detail.creatorName());
        assertEquals("04/06/2026", detail.creationDate());
        assertEquals("Chờ xử lý", detail.status());
    }

    @Test
    void loadTableDataReturnsItemsWithResolvedMerchandiseNames() throws Exception {
        salesRequestDao.request = new SalesRequest(7, 10, LocalDate.of(2026, 6, 4), SalesRequestStatus.PENDING);
        salesRequestItemDao.items = List.of(
                new SalesRequestItem(1, 7, "M001", 5, "pcs", LocalDate.of(2026, 6, 10)),
                new SalesRequestItem(2, 7, "M002", 2, "box", LocalDate.of(2026, 6, 12))
        );
        merchandiseDao.items = List.of(
                new Merchandise("M001", "Keyboard", "pcs"),
                new Merchandise("M002", "Mouse", "box")
        );
        controller.loadRequestData();

        List<RequestItemTableRow> rows = controller.loadTableData();

        assertEquals(2, rows.size());
        assertEquals("Keyboard", rows.get(0).itemName());
        assertEquals("Mouse", rows.get(1).itemName());
    }

    @Test
    void getStatusLabelReturnsVietnameseLabelsForAllStatuses() {
        assertEquals("Chờ xử lý", controller.getStatusLabel(SalesRequestStatus.PENDING));
        assertEquals("Đang xử lý", controller.getStatusLabel(SalesRequestStatus.PROCESSING));
        assertEquals("Hoàn tất", controller.getStatusLabel(SalesRequestStatus.COMPLETED));
        assertEquals("Lỗi", controller.getStatusLabel(SalesRequestStatus.ERROR));
    }

    @Test
    void getSalesRequestStatusReturnsLoadedRequestStatus() throws Exception {
        salesRequestDao.request = new SalesRequest(7, 10, LocalDate.of(2026, 6, 4), SalesRequestStatus.ERROR);

        controller.loadRequestData();

        assertEquals(SalesRequestStatus.ERROR, controller.getSalesRequestStatus());
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

    private static class FakeUserDao implements IUserDao {
        @Override
        public Optional<User> findById(int userId) {
            return Optional.of(new User(userId, "sales", "pw", "Nguyen Van Sales",
                    "sales@example.com", UserRole.SALES, LocalDate.now(), true));
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

    private static class FakeSalesRequestItemDao implements ISalesRequestItemDao {
        private List<SalesRequestItem> items = List.of();

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
            return 1;
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
            return true;
        }

        @Override
        public void deleteByRequestId(int requestId) {
        }

        @Override
        public void deleteByRequestId(Connection connection, int requestId) {
        }
    }

    private static class FakeMerchandiseDao implements IMerchandiseDao {
        private List<Merchandise> items = List.of();

        @Override
        public Optional<Merchandise> findById(String merchandiseCode) {
            return items.stream()
                    .filter(item -> item.getMerchandiseCode().equals(merchandiseCode))
                    .findFirst();
        }

        @Override
        public List<Merchandise> findAll() {
            return items;
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
