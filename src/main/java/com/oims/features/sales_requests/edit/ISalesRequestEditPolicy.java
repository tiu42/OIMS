package com.oims.features.sales_requests.edit;

import com.oims.core.model.SalesRequestStatus;

public interface ISalesRequestEditPolicy {

    boolean isEditable(SalesRequestStatus status);

    String getBlockedMessage(SalesRequestStatus status);

    EditPermissionResult evaluate(SalesRequestStatus status);
}
