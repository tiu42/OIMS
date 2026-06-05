package com.oims.features.sales_requests.process.service;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.features.sales_requests.process.dto.SalesRequestDTO;
import com.oims.features.sales_requests.process.dto.ItemDemand;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessRequestService implements IProcessRequestService {
    private final ISalesRequestDao salesRequestDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final IUserDao userDao;
    private final IMerchandiseDao merchandiseDao;

    public ProcessRequestService() {
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
        this.userDao = DaoFactory.getUserDao();
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
    }

    public ProcessRequestService(ISalesRequestDao salesRequestDao, ISalesRequestItemDao salesRequestItemDao,
                                 IUserDao userDao, IMerchandiseDao merchandiseDao) {
        this.salesRequestDao = salesRequestDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.userDao = userDao;
        this.merchandiseDao = merchandiseDao;
    }

    @Override
    public Optional<SalesRequestDTO> getSalesRequest(int requestId) throws SQLException {
        return salesRequestDao.findById(requestId)
                .map(req -> {
                    String statusText = switch (req.getStatus()) {
                        case PENDING -> "Chờ xử lý";
                        case PROCESSING -> "Đang xử lý";
                        case COMPLETED -> "Hoàn tất";
                        case ERROR -> "Lỗi";
                    };
                    return new SalesRequestDTO(
                            req.getRequestId(),
                            req.getCreatedBy(),
                            req.getCreatedDate(),
                            statusText
                    );
                });
    }

    @Override
    public List<ItemDemand> getDemands(int requestId) throws SQLException {
        List<SalesRequestItem> items = salesRequestItemDao.findByRequestId(requestId);
        List<Merchandise> merchandises = merchandiseDao.findAll();
        Map<String, String> merchNameMap = merchandises.stream()
                .collect(Collectors.toMap(Merchandise::getMerchandiseCode, Merchandise::getMerchandiseName));

        List<ItemDemand> demands = new ArrayList<>();
        for (SalesRequestItem item : items) {
            String name = merchNameMap.getOrDefault(item.getMerchandiseCode(), "Không xác định");
            demands.add(new ItemDemand(
                    item.getMerchandiseCode(),
                    name,
                    item.getQuantityOrdered(),
                    item.getUnit()
            ));
        }
        return demands;
    }

    @Override
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

    @Override
    public boolean beginProcessing(int requestId) throws SQLException {
        Optional<SalesRequest> requestOpt = salesRequestDao.findById(requestId);
        if (requestOpt.isEmpty()) {
            return false;
        }

        SalesRequestStatus status = requestOpt.get().getStatus();
        if (status == SalesRequestStatus.PENDING) {
            return salesRequestDao.updateStatusIfCurrent(requestId, SalesRequestStatus.PENDING, SalesRequestStatus.PROCESSING);
        }
        return status == SalesRequestStatus.PROCESSING;
    }

    @Override
    public void cancelProcessing(int requestId) throws SQLException {
        salesRequestDao.updateStatusIfCurrent(requestId, SalesRequestStatus.PROCESSING, SalesRequestStatus.PENDING);
    }
}
