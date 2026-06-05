package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.SiteStockTransportDTO;
import com.oims.features.sales_requests.process.dto.ItemDemand;
import java.sql.SQLException;
import java.util.List;

public interface ISiteStockService {
    List<SiteStockTransportDTO> getSiteStockAndTransport(String merchandiseCode, String excludedSiteCode) throws SQLException;
    List<ItemDemand> getFailedDemands(List<ItemDemand> demands, String excludedSiteCode) throws SQLException;
}
