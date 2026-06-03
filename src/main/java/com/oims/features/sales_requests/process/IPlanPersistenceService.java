package com.oims.features.sales_requests.process;

import com.oims.core.model.User;
import java.sql.SQLException;

public interface IPlanPersistenceService {
    void savePlan(int requestId, User creator, PlanDTO plan, boolean hasErrors) throws SQLException;
}
