package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.ItemDemand;
import com.oims.features.sales_requests.process.dto.ItemConfig;
import com.oims.features.sales_requests.process.dto.SiteStockTransportDTO;
import com.oims.features.sales_requests.process.dto.PlanDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IPlanGenerationService {
    List<PlanDTO> generatePlans(
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs,
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock) throws SQLException;
}
