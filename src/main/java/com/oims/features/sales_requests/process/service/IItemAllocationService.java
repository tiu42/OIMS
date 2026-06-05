package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.ItemAllocationOption;
import com.oims.features.sales_requests.process.dto.ItemConfig;
import com.oims.features.sales_requests.process.dto.SiteStockTransportDTO;
import java.util.List;

public interface IItemAllocationService {
    List<ItemAllocationOption> generateOptions(
            String merchCode, 
            String merchName, 
            String unit, 
            int demand, 
            List<SiteStockTransportDTO> siteStockList, 
            ItemConfig config);
}
