package com.oims.features.sales_requests.process.service;

import com.oims.core.model.DeliveryMeans;
import com.oims.features.sales_requests.process.dto.AllocatedItemAllocation;
import com.oims.features.sales_requests.process.dto.ItemAllocationOption;
import com.oims.features.sales_requests.process.dto.ItemConfig;
import com.oims.features.sales_requests.process.dto.SiteStockTransportDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemAllocationService {

    public List<ItemAllocationOption> generateOptions(
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
}
