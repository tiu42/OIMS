package com.oims.features.sales_requests.process.service;

import com.oims.core.model.DeliveryMeans;
import com.oims.features.sales_requests.process.dto.*;

import java.util.*;
import java.util.stream.Collectors;

public class PlanEvaluationService implements IPlanEvaluationService {

    @Override
    public PlanDTO evaluate(
            int planId, 
            List<ItemAllocationOption> combination, 
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs, 
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock,
            Map<String, String> siteNameMap) {
        
        // Group allocations by (siteCode, deliveryMeans)
        Map<SiteDeliveryKey, List<AllocatedItemAllocation>> poGroups = new HashMap<>();
        
        int prefSitesMatched = 0;
        int prefDeliveryMatched = 0;
        int totalStockCount = 0;

        for (int i = 0; i < demands.size(); i++) {
            ItemDemand demand = demands.get(i);
            ItemConfig config = configs.getOrDefault(demand.merchandiseCode(), new ItemConfig(null, null, DeliveryMeans.SHIP_DELIVERY));
            ItemAllocationOption option = combination.get(i);

            for (AllocatedItemAllocation allocation : option.allocations()) {
                String siteCode = allocation.siteCode();
                DeliveryMeans chosenDelivery = allocation.deliveryMeans();

                // Match site preferences
                if (siteCode.equals(config.preferredSite())) {
                    prefSitesMatched++;
                }
                
                // Match delivery preferences
                if (config.preferredDelivery() != null && config.preferredDelivery() == chosenDelivery) {
                    prefDeliveryMatched++;
                }

                // Total stock count sum for rank
                List<SiteStockTransportDTO> stocks = cachedSiteStock.get(demand.merchandiseCode());
                if (stocks != null) {
                    for (SiteStockTransportDTO s : stocks) {
                        if (s.siteCode().equals(siteCode)) {
                            totalStockCount += s.inStock();
                        }
                    }
                }

                SiteDeliveryKey key = new SiteDeliveryKey(siteCode, chosenDelivery);
                poGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(allocation);
            }
        }

        // Create AllocatedOrders
        List<AllocatedOrder> orders = new ArrayList<>();
        Set<String> uniqueSites = new HashSet<>();
        for (Map.Entry<SiteDeliveryKey, List<AllocatedItemAllocation>> entry : poGroups.entrySet()) {
            String siteCode = entry.getKey().siteCode();
            DeliveryMeans dm = entry.getKey().deliveryMeans();
            uniqueSites.add(siteCode);

            String siteName = siteNameMap.getOrDefault(siteCode, "Không xác định");
            List<AllocatedItem> orderItems = entry.getValue().stream()
                    .map(a -> new AllocatedItem(a.merchandiseCode(), a.merchandiseName(), a.quantity(), a.unit()))
                    .collect(Collectors.toList());

            orders.add(new AllocatedOrder(siteCode, siteName, dm, orderItems));
        }

        return new PlanDTO(
                planId,
                orders,
                uniqueSites.size(),
                prefSitesMatched,
                prefDeliveryMatched,
                totalStockCount
        );
    }

    private record SiteDeliveryKey(String siteCode, DeliveryMeans deliveryMeans) {}
}
