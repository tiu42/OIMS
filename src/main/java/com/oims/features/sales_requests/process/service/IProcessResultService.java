package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.ProcessedErrorDTO;
import com.oims.features.sales_requests.process.dto.SalesRequestDTO;
import com.oims.features.sales_requests.process.dto.PurchaseOrderDTO;
import com.oims.features.sales_requests.process.dto.AllocatedItem;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IProcessResultService {
    Optional<SalesRequestDTO> getSalesRequest(int requestId) throws SQLException;
    List<PurchaseOrderDTO> getPurchaseOrders(int requestId) throws SQLException;
    List<AllocatedItem> getPurchaseOrderItems(int orderId) throws SQLException;
    List<ProcessedErrorDTO> getProcessingErrors(int requestId) throws SQLException;
    String getCreatorName(int userId);
    String resolveMerchandiseName(String merchandiseCode);
    String resolveSiteName(String siteCode);
}
