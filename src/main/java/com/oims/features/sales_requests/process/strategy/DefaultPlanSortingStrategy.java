package com.oims.features.sales_requests.process.strategy;

import com.oims.features.sales_requests.process.dto.PlanDTO;
import java.util.List;

public class DefaultPlanSortingStrategy implements PlanSortingStrategy {
    @Override
    public void sort(List<PlanDTO> plans) {
        plans.sort((p1, p2) -> {
            if (p1.prefSitesMatched() != p2.prefSitesMatched()) {
                return Integer.compare(p2.prefSitesMatched(), p1.prefSitesMatched()); // More matched is better
            }
            if (p1.prefDeliveryMatched() != p2.prefDeliveryMatched()) {
                return Integer.compare(p2.prefDeliveryMatched(), p1.prefDeliveryMatched()); // More matched is better
            }
            if (p1.uniqueSitesCount() != p2.uniqueSitesCount()) {
                return Integer.compare(p1.uniqueSitesCount(), p2.uniqueSitesCount()); // Fewer unique sites is better
            }
            return Integer.compare(p2.totalStockCount(), p1.totalStockCount()); // More stock is better
        });
    }
}
