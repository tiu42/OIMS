package com.oims.features.sales_requests.process.service;

public class ServiceFactory {
    private static IPlanGenerationService planGenerationService = new PlanGenerationService();
    private static IPlanPersistenceService planPersistenceService = new PlanPersistenceService();
    private static ISiteStockService siteStockService = new SiteStockService();
    private static IProcessRequestService processRequestService = new ProcessRequestService();

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

    public static ISiteStockService getSiteStockService() {
        return siteStockService;
    }

    public static void setSiteStockService(ISiteStockService service) {
        siteStockService = service;
    }

    public static IProcessRequestService getProcessRequestService() {
        return processRequestService;
    }

    public static void setProcessRequestService(IProcessRequestService service) {
        processRequestService = service;
    }
}
