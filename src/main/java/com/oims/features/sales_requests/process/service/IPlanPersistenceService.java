package com.oims.features.sales_requests.process.service;

import com.oims.features.sales_requests.process.dto.PlanDTO;
import java.sql.SQLException;

public interface IPlanPersistenceService {
    void savePlan(int requestId, int creatorUserId, PlanDTO plan, boolean hasErrors) throws SQLException;
}
