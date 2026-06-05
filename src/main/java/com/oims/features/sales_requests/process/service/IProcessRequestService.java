package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.SalesRequestDTO;
import com.oims.features.sales_requests.process.dto.ItemDemand;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IProcessRequestService {
    Optional<SalesRequestDTO> getSalesRequest(int requestId) throws SQLException;
    List<ItemDemand> getDemands(int requestId) throws SQLException;
    String getCreatorName(int userId);
    boolean beginProcessing(int requestId) throws SQLException;
    void cancelProcessing(int requestId) throws SQLException;
}
