package com.oims.features.sales_requests.detail;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.core.session.AppSession;
import com.oims.features.sales_requests.edit.EditPermissionResult;
import com.oims.features.sales_requests.edit.EditServiceFactory;
import com.oims.features.sales_requests.edit.IEditRequestService;
import com.oims.features.sales_requests.edit.ISalesRequestEditPolicy;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RequestDetailController {
    private Optional<SalesRequest> salesRequest;
    private final ISalesRequestDao salesRequestDao;
    private final IUserDao userDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final IMerchandiseDao merchandiseDao;
    private final IEditRequestService editRequestService;
    private final ISalesRequestEditPolicy editPolicy;

    public RequestDetailController() {
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
        this.userDao = DaoFactory.getUserDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
        this.editRequestService = EditServiceFactory.getEditRequestService();
        this.editPolicy = EditServiceFactory.getEditPolicy();
    }

    public RequestDetailController(
            ISalesRequestDao salesRequestDao,
            IUserDao userDao,
            ISalesRequestItemDao salesRequestItemDao,
            IMerchandiseDao merchandiseDao,
            IEditRequestService editRequestService,
            ISalesRequestEditPolicy editPolicy
    ) {
        this.salesRequestDao = salesRequestDao;
        this.userDao = userDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.merchandiseDao = merchandiseDao;
        this.editRequestService = editRequestService;
        this.editPolicy = editPolicy;
    }

    public RequestDetailDTO loadRequestData() throws SQLException {
        salesRequest = salesRequestDao.findById(AppSession.getInstance().getSelectedRequestId());
        String id = salesRequest.get().getRequestId().toString();
        Map<Integer, String> creatorCache = new HashMap<>();
        String creatorName = resolveCreatorName(salesRequest.get().getCreatedBy(),creatorCache);
        String status = getStatusLabel(salesRequest.get().getStatus());
        String createdDate = salesRequest.get().getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        RequestDetailDTO requestDetailDTO = new RequestDetailDTO(id,creatorName,createdDate,status);
        return requestDetailDTO;
    }

    public List<RequestItemTableRow> loadTableData() throws SQLException {
        List<SalesRequestItem> salesRequestItems = salesRequestItemDao.findByRequestId(salesRequest.get().getRequestId());
        Map<String, String> merchNameCache = new HashMap<>();
        List<RequestItemTableRow> rows = new ArrayList<>();

        for (SalesRequestItem item : salesRequestItems) {
            rows.add(new RequestItemTableRow(item, resolveItemName(item.getMerchandiseCode(),merchNameCache)));
        }

        return rows;
    }

    private String resolveCreatorName(Integer userId, Map<Integer, String> cache) {
        if (userId == null) {return "Không xác định";}
        if (cache.containsKey(userId)) {return cache.get(userId);}
        try {
            String creatorName = userDao.findById(userId)
                    .map(this::formatUserName)
                    .orElse("Người dùng #" + userId);
            cache.put(userId, creatorName);
            return creatorName;
        } catch (SQLException exception) {
            String fallback = "Người dùng #" + userId;
            cache.put(userId, fallback);
            return fallback;
        }
    }
    private String formatUserName(User user) {
        if (user == null) {return "Không xác định";}
        String fullName = user.getFullName();
        if (fullName != null && !fullName.isBlank()) {return fullName;}

        return user.getUsername() == null || user.getUsername().isBlank() ? "Không xác định" : user.getUsername();
    }
    public String getStatusLabel(SalesRequestStatus status) {
        if (status == null) {
            return "Không xác định";
        }

        return switch (status) {
            case PENDING -> "Chờ xử lý";
            case PROCESSING -> "Đang xử lý";
            case COMPLETED -> "Hoàn tất";
            case ERROR -> "Lỗi";
        };
    }
    private String resolveItemName(String itemId, Map<String,String> cache){
        if (itemId == null) {return "Không xác định";}
        if (cache.containsKey(itemId)) {return cache.get(itemId);}
        try {
            String itemName = merchandiseDao.findById(itemId)
                    .map(this::formatItemName)
                    .orElse("");
            cache.put(itemId, itemName);
            return itemName;
        } catch (SQLException exception) {
            String fallback = itemId;
            cache.put(itemId, fallback);
            return fallback;
        }
    }
    private String formatItemName(Merchandise merchandise){
        if (merchandise == null) {return "Không xác định";}
        String itemName = merchandise.getMerchandiseName();
        if (itemName != null && !itemName.isBlank()) {return itemName;}

        return "Khong xac dinh";
    }
    public SalesRequestStatus getSalesRequestStatus() {
        return salesRequest.map(SalesRequest::getStatus).orElse(null);
    }

    public boolean canEditRequest() {
        return editPolicy.isEditable(getSalesRequestStatus());
    }

    public EditPermissionResult checkEditPermission() throws SQLException {
        Integer requestId = AppSession.getInstance().getSelectedRequestId();
        if (requestId == null) {
            return EditPermissionResult.notFound();
        }
        return editRequestService.checkEditPermission(requestId);
    }

    public record RequestDetailDTO(String id, String creatorName, String creationDate, String status){}
}
