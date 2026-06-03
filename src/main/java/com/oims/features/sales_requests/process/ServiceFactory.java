package com.oims.features.sales_requests.process;

public class ServiceFactory {
    private static IPlanGenerationService planGenerationService = new PlanGenerationService();
    private static IPlanPersistenceService planPersistenceService = new PlanPersistenceService();

    public static IPlanGenerationService getPlanGenerationService() {
        return planGenerationService;
    }

    public static void setPlanGenerationService(IPlanGenerationService service) {
        planGenerationService = service;
    }

    public static IPlanPersistenceService getPlanPersistenceService() {
        return planPersistenceService;
    }

    public static void setPlanPersistenceService(IPlanPersistenceService service) {
        planPersistenceService = service;
    }
}
