package com.oims.features.sales_requests.process;

import java.util.List;

public interface PlanSortingStrategy {
    void sort(List<PlanDTO> plans);
}
