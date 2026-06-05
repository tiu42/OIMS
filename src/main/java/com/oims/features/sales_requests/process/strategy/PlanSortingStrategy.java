package com.oims.features.sales_requests.process.strategy;

import com.oims.features.sales_requests.process.dto.PlanDTO;
import java.util.List;

public interface PlanSortingStrategy {
    void sort(List<PlanDTO> plans);
}
