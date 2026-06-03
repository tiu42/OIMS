package com.oims.features.sales_requests.process;

import java.util.List;

public record PlanDTO(
    int id,
    List<AllocatedOrder> orders,
    int uniqueSitesCount,
    int prefSitesMatched,
    int prefDeliveryMatched,
    int totalStockCount
) {
    public String getPlanName() { return "Phương án " + id; }
    public String getSitesCountDisplay() { return uniqueSitesCount + " site"; }
    public String getPrefSitesDisplay() { return prefSitesMatched + " mặt hàng"; }
    public String getPrefDeliveryDisplay() { return prefDeliveryMatched + " mặt hàng"; }
    public String getTotalStockDisplay() { return String.format("%,d", totalStockCount); }
}
