package com.oims.features.sales_requests.edit;

import com.oims.core.model.Merchandise;
import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestItem;

import java.util.List;

public record EditFormData(
        SalesRequest salesRequest,
        String creatorName,
        List<SalesRequestItem> items,
        List<Merchandise> merchandises
) {
}
