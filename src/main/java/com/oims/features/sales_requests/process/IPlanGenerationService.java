package com.oims.features.sales_requests.process;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IPlanGenerationService {
    List<PlanDTO> generatePlans(
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs,
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock) throws SQLException;
}
