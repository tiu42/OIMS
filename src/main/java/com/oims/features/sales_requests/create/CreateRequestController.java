package com.oims.features.sales_requests.create;

import com.oims.core.dao.*;
import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;
import com.oims.features.sales_requests.dto.RequestItemDTO;
import com.oims.features.sales_requests.shared.RequestItemValidator;

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

    public void saveSalesRequest(User creator, List<RequestItemDTO> items) throws SQLException, IllegalArgumentException {
        if (creator == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin người tạo. Vui lòng đăng nhập lại.");
        }
        RequestItemValidator.validateAll(items);

        SalesRequest salesRequest = new SalesRequest();
        salesRequest.setCreatedBy(creator.getUserId());
        salesRequest.setCreatedDate(LocalDate.now());
        salesRequest.setStatus(SalesRequestStatus.PENDING);

        int requestId = salesRequestDao.insert(salesRequest);
        if (requestId <= 0) {
            throw new SQLException("Không thể tạo yêu cầu nhập hàng.");
        }

        for (RequestItemDTO item : items) {
            SalesRequestItem salesRequestItem = new SalesRequestItem();
            salesRequestItem.setRequestId(requestId);
            salesRequestItem.setMerchandiseCode(item.merchandiseCode());
            salesRequestItem.setQuantityOrdered(item.quantity());
            salesRequestItem.setUnit(item.unit());
            salesRequestItem.setDesiredDeliveryDate(item.desiredDate());

            salesRequestItemDao.insert(salesRequestItem);
        }
    }
}
