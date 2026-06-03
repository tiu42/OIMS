package com.oims.features.sales_requests.process;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.ImportSite;
import com.oims.core.dao.IImportSiteDao;
import com.oims.core.dao.DaoFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PlanGenerationService implements IPlanGenerationService {
    private final IImportSiteDao importSiteDao;
    private final PlanSortingStrategy sortingStrategy;

    public PlanGenerationService() {
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.sortingStrategy = new DefaultPlanSortingStrategy();
    }

    public PlanGenerationService(IImportSiteDao importSiteDao, PlanSortingStrategy sortingStrategy) {
        this.importSiteDao = importSiteDao;
        this.sortingStrategy = sortingStrategy;
    }

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

            ItemConfig config = configs.getOrDefault(demand.merchandiseCode(), new ItemConfig(null, null, DeliveryMeans.SHIP_DELIVERY));
            List<ItemAllocationOption> options = generateItemAllocationOptions(
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
            // Group allocations by (siteCode, deliveryMeans)
            Map<SiteDeliveryKey, List<AllocatedItemAllocation>> poGroups = new HashMap<>();
            
            int prefSitesMatched = 0;
            int prefDeliveryMatched = 0;
            int totalStockCount = 0;

            for (int i = 0; i < demands.size(); i++) {
                ItemDemand demand = demands.get(i);
                ItemConfig config = configs.getOrDefault(demand.merchandiseCode(), new ItemConfig(null, null, DeliveryMeans.SHIP_DELIVERY));
                ItemAllocationOption option = combo.get(i);

                for (AllocatedItemAllocation allocation : option.allocations()) {
                    String siteCode = allocation.siteCode();
                    DeliveryMeans chosenDelivery = allocation.deliveryMeans();

                    // Match site preferences
                    if (siteCode.equals(config.preferredSite())) {
                        prefSitesMatched++;
                    }
                    
                    // Match delivery preferences
                    if (config.preferredDelivery() != null && config.preferredDelivery() == chosenDelivery) {
                        prefDeliveryMatched++;
                    }

                    // Total stock count sum for rank
                    List<SiteStockTransportDTO> stocks = cachedSiteStock.get(demand.merchandiseCode());
                    if (stocks != null) {
                        for (SiteStockTransportDTO s : stocks) {
                            if (s.siteCode().equals(siteCode)) {
                                totalStockCount += s.inStock();
                            }
                        }
                    }

                    SiteDeliveryKey key = new SiteDeliveryKey(siteCode, chosenDelivery);
                    poGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(allocation);
                }
            }

            // Create AllocatedOrders
            List<AllocatedOrder> orders = new ArrayList<>();
            Set<String> uniqueSites = new HashSet<>();
            for (Map.Entry<SiteDeliveryKey, List<AllocatedItemAllocation>> entry : poGroups.entrySet()) {
                String siteCode = entry.getKey().siteCode();
                DeliveryMeans dm = entry.getKey().deliveryMeans();
                uniqueSites.add(siteCode);

                String siteName = siteNameMap.getOrDefault(siteCode, "Không xác định");
                List<AllocatedItem> orderItems = entry.getValue().stream()
                        .map(a -> new AllocatedItem(a.merchandiseCode(), a.merchandiseName(), a.quantity(), a.unit()))
                        .collect(Collectors.toList());

                orders.add(new AllocatedOrder(siteCode, siteName, dm, orderItems));
            }

            plans.add(new PlanDTO(
                    planIndex++,
                    orders,
                    uniqueSites.size(),
                    prefSitesMatched,
                    prefDeliveryMatched,
                    totalStockCount
            ));
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

    private List<ItemAllocationOption> generateItemAllocationOptions(
            String merchCode, String merchName, String unit, int demand, 
            List<SiteStockTransportDTO> siteStockList, ItemConfig config) {
        
        List<ItemAllocationOption> options = new ArrayList<>();
        int n = siteStockList.size();
        int totalSubsets = 1 << n;
        
        String prefSite = config != null ? config.preferredSite() : null;
        String nonPrefSite = config != null ? config.nonPreferredSite() : null;
        
        for (int i = 1; i < totalSubsets; i++) {
            List<SiteStockTransportDTO> subset = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(siteStockList.get(j));
                }
            }
            
            // Sort subset by priority
            subset.sort((s1, s2) -> {
                boolean p1 = s1.siteCode().equals(prefSite);
                boolean p2 = s2.siteCode().equals(prefSite);
                if (p1 && !p2) return -1;
                if (!p1 && p2) return 1;
                
                boolean np1 = s1.siteCode().equals(nonPrefSite);
                boolean np2 = s2.siteCode().equals(nonPrefSite);
                if (np1 && !np2) return 1;
                if (!np1 && np2) return -1;
                
                return Integer.compare(s2.inStock(), s1.inStock()); // Descending stock
            });
            
            int remaining = demand;
            List<AllocatedItemAllocation> allocations = new ArrayList<>();
            boolean isMinimal = true;
            
            for (SiteStockTransportDTO site : subset) {
                if (remaining <= 0) {
                    isMinimal = false;
                    break;
                }
                int allocated = Math.min(remaining, site.inStock());
                allocations.add(new AllocatedItemAllocation(site.siteCode(), merchCode, merchName, allocated, unit, null));
                remaining -= allocated;
            }
            
            if (remaining == 0 && isMinimal) {
                generateDeliveryCombinations(0, allocations, siteStockList, options);
            }
        }
        return options;
    }

    private void generateDeliveryCombinations(
            int index, List<AllocatedItemAllocation> currentAllocations, 
            List<SiteStockTransportDTO> siteStockList, 
            List<ItemAllocationOption> options) {
        
        if (index == currentAllocations.size()) {
            options.add(new ItemAllocationOption(new ArrayList<>(currentAllocations)));
            return;
        }
        
        AllocatedItemAllocation alloc = currentAllocations.get(index);
        SiteStockTransportDTO siteInfo = siteStockList.stream()
                .filter(s -> s.siteCode().equals(alloc.siteCode()))
                .findFirst()
                .orElse(null);
        
        List<DeliveryMeans> supported = new ArrayList<>();
        if (siteInfo != null) {
            if (siteInfo.shipDays() > 0) {
                supported.add(DeliveryMeans.SHIP_DELIVERY);
            }
            if (siteInfo.airDays() > 0) {
                supported.add(DeliveryMeans.AIR_DELIVERY);
            }
        }
        
        if (supported.isEmpty()) {
            supported.add(DeliveryMeans.SHIP_DELIVERY);
        }
        
        for (DeliveryMeans dm : supported) {
            AllocatedItemAllocation newAlloc = new AllocatedItemAllocation(
                    alloc.siteCode(),
                    alloc.merchandiseCode(),
                    alloc.merchandiseName(),
                    alloc.quantity(),
                    alloc.unit(),
                    dm
            );
            currentAllocations.set(index, newAlloc);
            generateDeliveryCombinations(index + 1, currentAllocations, siteStockList, options);
        }
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

    private record SiteDeliveryKey(String siteCode, DeliveryMeans deliveryMeans) {}
}
