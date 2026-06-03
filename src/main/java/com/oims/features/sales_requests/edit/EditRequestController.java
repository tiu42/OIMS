package com.oims.features.sales_requests.edit;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.features.sales_requests.create.CreateRequestController.TempRequestItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EditRequestController {
    private final IMerchandiseDao merchandiseDao;
    private final ISalesRequestDao salesRequestDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final IUserDao userDao;

    public EditRequestController() {
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
        this.userDao = DaoFactory.getUserDao();
    }

    public EditRequestController(IMerchandiseDao merchandiseDao, ISalesRequestDao salesRequestDao, ISalesRequestItemDao salesRequestItemDao, IUserDao userDao) {
        this.merchandiseDao = merchandiseDao;
        this.salesRequestDao = salesRequestDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.userDao = userDao;
    }

    public List<Merchandise> getAllMerchandises() throws SQLException {
        return merchandiseDao.findAll();
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

    public void updateSalesRequest(int requestId, User modifier, List<TempRequestItem> tempItems) throws SQLException, IllegalArgumentException {
        if (modifier == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin người chỉnh sửa. Vui lòng đăng nhập lại.");
        }
        if (tempItems == null || tempItems.isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng yêu cầu không được để trống.");
        }

        // Validate items
        for (TempRequestItem item : tempItems) {
            if (item.merchandiseCode() == null || item.merchandiseCode().isBlank()) {
                throw new IllegalArgumentException("Mã mặt hàng không hợp lệ.");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Số lượng mặt hàng phải lớn hơn 0.");
            }
            if (item.unit() == null || item.unit().isBlank()) {
                throw new IllegalArgumentException("Đơn vị mặt hàng không được để trống.");
            }
            if (item.desiredDate() == null) {
                throw new IllegalArgumentException("Ngày nhận mong muốn không được để trống.");
            }
            if (item.desiredDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày nhận mong muốn không được ở trong quá khứ.");
            }
        }

        // Load and check the request status
        Optional<SalesRequest> requestOpt = salesRequestDao.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu nhập hàng không tồn tại.");
        }

        SalesRequest salesRequest = requestOpt.get();
        if (salesRequest.getStatus() != SalesRequestStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể chỉnh sửa yêu cầu ở trạng thái Chờ xử lý.");
        }

        // Delete existing items
        salesRequestItemDao.deleteByRequestId(requestId);

        // Insert new items
        for (TempRequestItem tempItem : tempItems) {
            SalesRequestItem salesRequestItem = new SalesRequestItem();
            salesRequestItem.setRequestId(requestId);
            salesRequestItem.setMerchandiseCode(tempItem.merchandiseCode());
            salesRequestItem.setQuantityOrdered(tempItem.quantity());
            salesRequestItem.setUnit(tempItem.unit());
            salesRequestItem.setDesiredDeliveryDate(tempItem.desiredDate());

            salesRequestItemDao.insert(salesRequestItem);
        }
    }
}
