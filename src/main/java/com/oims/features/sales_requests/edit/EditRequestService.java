package com.oims.features.sales_requests.edit;

import com.oims.core.dao.IMerchandiseDao;
import com.oims.core.dao.ISalesRequestDao;
import com.oims.core.dao.ISalesRequestItemDao;
import com.oims.core.dao.IUserDao;
import com.oims.core.database.DBConnection;
import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;
import com.oims.core.model.User;
import com.oims.features.sales_requests.dto.RequestItemDTO;
import com.oims.features.sales_requests.shared.RequestItemValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EditRequestService implements IEditRequestService {

    private final IMerchandiseDao merchandiseDao;
    private final ISalesRequestDao salesRequestDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final IUserDao userDao;
    private final ISalesRequestEditPolicy editPolicy;

    public EditRequestService(
            IMerchandiseDao merchandiseDao,
            ISalesRequestDao salesRequestDao,
            ISalesRequestItemDao salesRequestItemDao,
            IUserDao userDao,
            ISalesRequestEditPolicy editPolicy
    ) {
        this.merchandiseDao = merchandiseDao;
        this.salesRequestDao = salesRequestDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.userDao = userDao;
        this.editPolicy = editPolicy;
    }

    @Override
    public EditPermissionResult checkEditPermission(int requestId) throws SQLException {
        Optional<SalesRequest> requestOpt = salesRequestDao.findById(requestId);
        if (requestOpt.isEmpty()) {
            return EditPermissionResult.notFound();
        }
        return editPolicy.evaluate(requestOpt.get().getStatus());
    }

    @Override
    public Optional<EditFormData> loadEditFormData(int requestId) throws SQLException {
        Optional<SalesRequest> requestOpt = salesRequestDao.findById(requestId);
        if (requestOpt.isEmpty()) {
            return Optional.empty();
        }

        SalesRequest salesRequest = requestOpt.get();
        List<SalesRequestItem> items = salesRequestItemDao.findByRequestId(requestId);
        List<Merchandise> merchandises = merchandiseDao.findAll();
        String creatorName = resolveCreatorName(salesRequest.getCreatedBy());

        return Optional.of(new EditFormData(salesRequest, creatorName, items, merchandises));
    }

    @Override
    public void updateSalesRequest(int requestId, User modifier, List<RequestItemDTO> items) throws SQLException {
        if (modifier == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin người chỉnh sửa. Vui lòng đăng nhập lại.");
        }
        RequestItemValidator.validateAll(items);

        Optional<SalesRequest> requestOpt = salesRequestDao.findById(requestId);
        if (requestOpt.isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu nhập hàng không tồn tại.");
        }

        EditPermissionResult permission = editPolicy.evaluate(requestOpt.get().getStatus());
        if (!permission.canEdit()) {
            throw new IllegalArgumentException(permission.blockedMessage());
        }

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                salesRequestItemDao.deleteByRequestId(connection, requestId);

                for (RequestItemDTO item : items) {
                    SalesRequestItem salesRequestItem = new SalesRequestItem();
                    salesRequestItem.setRequestId(requestId);
                    salesRequestItem.setMerchandiseCode(item.merchandiseCode());
                    salesRequestItem.setQuantityOrdered(item.quantity());
                    salesRequestItem.setUnit(item.unit());
                    salesRequestItem.setDesiredDeliveryDate(item.desiredDate());
                    salesRequestItemDao.insert(connection, salesRequestItem);
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private String resolveCreatorName(Integer userId) {
        if (userId == null) {
            return "Không xác định";
        }

        try {
            return userDao.findById(userId)
                    .map(user -> {
                        String fullName = user.getFullName();
                        if (fullName != null && !fullName.isBlank()) {
                            return fullName;
                        }
                        return user.getUsername() == null || user.getUsername().isBlank()
                                ? "Không xác định"
                                : user.getUsername();
                    })
                    .orElse("Người dùng #" + userId);
        } catch (SQLException e) {
            return "Người dùng #" + userId;
        }
    }
}
