package com.oims.features.sales_requests.create;

import com.oims.core.dao.*;
import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CreateRequestController {
    private final IMerchandiseDao merchandiseDao;
    private final ISalesRequestDao salesRequestDao;
    private final ISalesRequestItemDao salesRequestItemDao;

    public CreateRequestController() {
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
    }

    public CreateRequestController(IMerchandiseDao merchandiseDao, ISalesRequestDao salesRequestDao, ISalesRequestItemDao salesRequestItemDao) {
        this.merchandiseDao = merchandiseDao;
        this.salesRequestDao = salesRequestDao;
        this.salesRequestItemDao = salesRequestItemDao;
    }

    public List<Merchandise> getAllMerchandises() throws SQLException {
        return merchandiseDao.findAll();
    }

    public void saveSalesRequest(User creator, List<TempRequestItem> tempItems) throws SQLException, IllegalArgumentException {
        if (creator == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin người tạo. Vui lòng đăng nhập lại.");
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

        // Insert SalesRequest
        SalesRequest salesRequest = new SalesRequest();
        salesRequest.setCreatedBy(creator.getUserId());
        salesRequest.setCreatedDate(LocalDate.now());
        salesRequest.setStatus(SalesRequestStatus.PENDING);

        int requestId = salesRequestDao.insert(salesRequest);
        if (requestId <= 0) {
            throw new SQLException("Không thể tạo yêu cầu nhập hàng.");
        }

        // Insert items
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

    public record TempRequestItem(String merchandiseCode, int quantity, String unit, LocalDate desiredDate) {}
}
