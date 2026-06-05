package com.oims.features.sales_requests.edit;

import com.oims.core.model.User;
import com.oims.features.sales_requests.dto.RequestItemDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IEditRequestService {

    EditPermissionResult checkEditPermission(int requestId) throws SQLException;

    Optional<EditFormData> loadEditFormData(int requestId) throws SQLException;

    void updateSalesRequest(int requestId, User modifier, List<RequestItemDTO> items) throws SQLException;
}
