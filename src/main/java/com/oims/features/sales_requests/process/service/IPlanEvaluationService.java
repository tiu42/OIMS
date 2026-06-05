package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.*;
import java.util.List;
import java.util.Map;

public interface IPlanEvaluationService {
    PlanDTO evaluate(
            int planId, 
            List<ItemAllocationOption> combination, 
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs, 
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock,
            Map<String, String> siteNameMap);
}
