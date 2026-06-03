package com.oims.features.sales_requests.detail;

import com.oims.core.model.SalesRequestItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record RequestItemTableRow(SalesRequestItem salesRequestItem, String itemName) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public String getItemId(){return salesRequestItem == null ? null : salesRequestItem.getMerchandiseCode();}
    public Integer getQuantity(){return salesRequestItem == null ? null : salesRequestItem.getQuantityOrdered();}
    public String getUnit(){return salesRequestItem == null ? null : salesRequestItem.getUnit();}
    public LocalDate getDesiredDate(){return salesRequestItem == null ? null : salesRequestItem.getDesiredDeliveryDate();}
    public String getDesiredDateDisplay(){
        LocalDate date = getDesiredDate();
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

}
