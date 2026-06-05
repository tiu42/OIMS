package com.oims.features.sales_requests.create;

import com.oims.core.dao.IMerchandiseDao;
import com.oims.core.dao.ISalesRequestDao;
import com.oims.core.dao.ISalesRequestItemDao;
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

class CreateRequestControllerTest {
    private FakeSalesRequestDao salesRequestDao;
    private FakeSalesRequestItemDao salesRequestItemDao;
    private CreateRequestController controller;

    @BeforeEach
    void setUp() {
        salesRequestDao = new FakeSalesRequestDao();
        salesRequestItemDao = new FakeSalesRequestItemDao();
        controller = new CreateRequestController(new FakeMerchandiseDao(), salesRequestDao, salesRequestItemDao);
    }

    @Test
    void saveSalesRequestWithValidItemsCreatesPendingRequestAndItems() throws SQLException {
        User creator = createUser(10);

        controller.saveSalesRequest(creator, List.of(
                new RequestItemDTO("M001", 5, "pcs", LocalDate.now().plusDays(3)),
                new RequestItemDTO("M002", 2, "box", LocalDate.now().plusDays(5))
        ));

        assertEquals(1, salesRequestDao.savedRequests.size());
        assertEquals(10, salesRequestDao.savedRequests.get(0).getCreatedBy());
        assertEquals(SalesRequestStatus.PENDING, salesRequestDao.savedRequests.get(0).getStatus());
        assertEquals(2, salesRequestItemDao.savedItems.size());
        assertEquals(1, salesRequestItemDao.savedItems.get(0).getRequestId());
    }

    @Test
    void saveSalesRequestRejectsMissingCreator() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.saveSalesRequest(null, List.of(
                        new RequestItemDTO("M001", 5, "pcs", LocalDate.now().plusDays(3))
                ))
        );

        assertEquals("Không tìm thấy thông tin người tạo. Vui lòng đăng nhập lại.", exception.getMessage());
        assertEquals(0, salesRequestDao.savedRequests.size());
    }

    @Test
    void saveSalesRequestRejectsEmptyItemList() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.saveSalesRequest(createUser(10), List.of())
        );

        assertEquals("Danh sách mặt hàng yêu cầu không được để trống.", exception.getMessage());
    }

    @Test
    void saveSalesRequestRejectsNonPositiveQuantity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.saveSalesRequest(createUser(10), List.of(
                        new RequestItemDTO("M001", 0, "pcs", LocalDate.now().plusDays(3))
                ))
        );

        assertEquals("Số lượng mặt hàng phải lớn hơn 0.", exception.getMessage());
    }

    @Test
    void saveSalesRequestRejectsPastDesiredDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                controller.saveSalesRequest(createUser(10), List.of(
                        new RequestItemDTO("M001", 5, "pcs", LocalDate.now().minusDays(1))
                ))
        );

        assertEquals("Ngày nhận mong muốn không được ở trong quá khứ.", exception.getMessage());
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
        private final List<SalesRequest> savedRequests = new ArrayList<>();
        private int nextId = 1;

        @Override
        public Optional<SalesRequest> findById(int requestId) {
            return savedRequests.stream()
                    .filter(request -> request.getRequestId() == requestId)
                    .findFirst();
        }

        @Override
        public List<SalesRequest> findAll() {
            return savedRequests;
        }

        @Override
        public List<SalesRequest> findByCreatedBy(int createdBy) {
            return savedRequests.stream()
                    .filter(request -> request.getCreatedBy() == createdBy)
                    .toList();
        }

        @Override
        public int insert(SalesRequest salesRequest) {
            salesRequest.setRequestId(nextId++);
            savedRequests.add(salesRequest);
            return salesRequest.getRequestId();
        }

        @Override
        public boolean update(SalesRequest salesRequest) {
            return true;
        }

        @Override
        public boolean delete(int requestId) {
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

    private static class FakeSalesRequestItemDao implements ISalesRequestItemDao {
        private final List<SalesRequestItem> savedItems = new ArrayList<>();

        @Override
        public Optional<SalesRequestItem> findById(int itemId) {
            return Optional.empty();
        }

        @Override
        public List<SalesRequestItem> findAll() {
            return savedItems;
        }

        @Override
        public List<SalesRequestItem> findByRequestId(int requestId) {
            return savedItems.stream()
                    .filter(item -> item.getRequestId() == requestId)
                    .toList();
        }

        @Override
        public int insert(SalesRequestItem item) {
            savedItems.add(item);
            return savedItems.size();
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
            savedItems.removeIf(item -> item.getRequestId() == requestId);
        }

        @Override
        public void deleteByRequestId(Connection connection, int requestId) {
            deleteByRequestId(requestId);
        }
    }
}
