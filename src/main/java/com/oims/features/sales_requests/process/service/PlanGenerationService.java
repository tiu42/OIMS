package com.oims.features.sales_requests.process.service;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.ImportSite;
import com.oims.core.dao.IImportSiteDao;
import com.oims.core.dao.DaoFactory;
import com.oims.features.sales_requests.process.dto.*;
import com.oims.features.sales_requests.process.strategy.PlanSortingStrategy;
import com.oims.features.sales_requests.process.strategy.DefaultPlanSortingStrategy;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PlanGenerationService implements IPlanGenerationService {
    private final IImportSiteDao importSiteDao;
    private final PlanSortingStrategy sortingStrategy;
    private final ItemAllocationService allocationService;
    private final PlanEvaluationService evaluationService;

    public PlanGenerationService() {
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.sortingStrategy = new DefaultPlanSortingStrategy();
        this.allocationService = new ItemAllocationService();
        this.evaluationService = new PlanEvaluationService();
    }

    public PlanGenerationService(IImportSiteDao importSiteDao, 
                                 PlanSortingStrategy sortingStrategy) {
        this.importSiteDao = importSiteDao;
        this.sortingStrategy = sortingStrategy;
        this.allocationService = new ItemAllocationService();
        this.evaluationService = new PlanEvaluationService();
    }

    public PlanGenerationService(IImportSiteDao importSiteDao, 
                                 PlanSortingStrategy sortingStrategy,
                                 ItemAllocationService allocationService,
                                 PlanEvaluationService evaluationService) {
        this.importSiteDao = importSiteDao;
        this.sortingStrategy = sortingStrategy;
        this.allocationService = allocationService;
        this.evaluationService = evaluationService;
    }

    @Override
    public List<PlanDTO> generatePlans(
            List<ItemDemand> demands, 
            Map<String, ItemConfig> configs,
            Map<String, List<SiteStockTransportDTO>> cachedSiteStock) throws SQLException {
        
        List<List<ItemAllocationOption>> allItemsOptions = new ArrayList<>();
        for (ItemDemand demand : demands) {
            List<SiteStockTransportDTO> siteStockList = cachedSiteStock.get(demand.merchandiseCode());
            if (siteStockList == null) {
                siteStockList = Collections.emptyList();
            }

            ItemConfig config = configs.getOrDefault(demand.merchandiseCode(), new ItemConfig(null, null, DeliveryMeans.AIR_DELIVERY));
            List<ItemAllocationOption> options = allocationService.generateOptions(
                    demand.merchandiseCode(),
                    demand.merchandiseName(),
                    demand.unit(),
                    demand.quantity(),
                    siteStockList,
                    config
            );

            if (options.isEmpty()) {
                return Collections.emptyList();
            }
            allItemsOptions.add(options);
        }

        // Cartesian product search
        List<List<ItemAllocationOption>> planCombinations = new ArrayList<>();
        searchPlans(0, allItemsOptions, new ArrayList<>(), planCombinations);

        // Fetch site details for naming
        List<ImportSite> allSites = importSiteDao.findAll();
        Map<String, String> siteNameMap = allSites.stream()
                .collect(Collectors.toMap(ImportSite::getSiteCode, ImportSite::getSiteName));

        List<PlanDTO> plans = new ArrayList<>();
        int planIndex = 1;
        for (List<ItemAllocationOption> combo : planCombinations) {
            plans.add(evaluationService.evaluate(planIndex++, combo, demands, configs, cachedSiteStock, siteNameMap));
        }

        // Sort plans based on strategy (OCP)
        sortingStrategy.sort(plans);

        // Re-assign plan IDs after sorting
        List<PlanDTO> sortedPlans = new ArrayList<>();
        for (int i = 0; i < plans.size(); i++) {
            PlanDTO old = plans.get(i);
            sortedPlans.add(new PlanDTO(
                    i + 1,
                    old.orders(),
                    old.uniqueSitesCount(),
                    old.prefSitesMatched(),
                    old.prefDeliveryMatched(),
                    old.totalStockCount()
            ));
        }

        return sortedPlans;
    }

    private void searchPlans(int itemIndex, List<List<ItemAllocationOption>> allItemsOptions, 
                              List<ItemAllocationOption> currentPlan, List<List<ItemAllocationOption>> results) {
        if (itemIndex == allItemsOptions.size()) {
            results.add(new ArrayList<>(currentPlan));
            return;
        }
        for (ItemAllocationOption option : allItemsOptions.get(itemIndex)) {
            currentPlan.add(option);
            searchPlans(itemIndex + 1, allItemsOptions, currentPlan, results);
            currentPlan.remove(currentPlan.size() - 1);
        }
    }
}
