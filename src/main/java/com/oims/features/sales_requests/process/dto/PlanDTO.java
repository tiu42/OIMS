package com.oims.features.sales_requests.process.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public String getSitesCountDisplay() { return orders.size() + " đơn"; }
    public String getPrefSitesDisplay() { return prefSitesMatched + " mặt hàng"; }
    public String getPrefDeliveryDisplay() { return prefDeliveryMatched + " mặt hàng"; }
    public String getTotalStockDisplay() { return String.format("%,d", totalStockCount); }

    public String getLatestReceiptDateDisplay() {
        if (orders == null || orders.isEmpty()) return "-";
        LocalDate maxDate = null;
        for (AllocatedOrder order : orders) {
            LocalDate date = order.expectedDeliveryDate();
            if (date != null) {
                if (maxDate == null || date.isAfter(maxDate)) {
                    maxDate = date;
                }
            }
        }
        if (maxDate == null) return "-";
        return maxDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
