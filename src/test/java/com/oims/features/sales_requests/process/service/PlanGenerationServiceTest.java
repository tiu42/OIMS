package com.oims.features.sales_requests.process.service;

import com.oims.core.dao.IImportSiteDao;
import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.ImportSite;
import com.oims.features.sales_requests.process.dto.*;
import com.oims.features.sales_requests.process.strategy.DefaultPlanSortingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanGenerationServiceTest {
    private PlanGenerationService service;

    @BeforeEach
    void setUp() {
        service = new PlanGenerationService(new FakeImportSiteDao(), new DefaultPlanSortingStrategy());
    }

    @Test
    void generatePlansReturnsEmptyListWhenAnyDemandCannotBeFulfilled() throws SQLException {
        List<PlanDTO> plans = service.generatePlans(
                List.of(new ItemDemand("M001", "Keyboard", 10, "pcs")),
                Map.of(),
                Map.of("M001", List.of(
                        new SiteStockTransportDTO("SITE01", "Tokyo Site", "Japan", 4, "pcs", 7, 2)
                ))
        );

        assertTrue(plans.isEmpty());
    }

    @Test
    void generatePlansCreatesPlanFromSingleSiteStock() throws SQLException {
        List<PlanDTO> plans = service.generatePlans(
                List.of(new ItemDemand("M001", "Keyboard", 10, "pcs")),
                Map.of("M001", new ItemConfig("SITE01", null, DeliveryMeans.SHIP_DELIVERY)),
                Map.of("M001", List.of(
                        new SiteStockTransportDTO("SITE01", "Tokyo Site", "Japan", 15, "pcs", 7, 2)
                ))
        );

        assertFalse(plans.isEmpty());
        PlanDTO firstPlan = plans.get(0);
        assertEquals(1, firstPlan.id());
        assertEquals(1, firstPlan.orders().size());
        assertEquals("SITE01", firstPlan.orders().get(0).siteCode());
        assertEquals(10, firstPlan.orders().get(0).items().get(0).quantity());
        assertEquals(1, firstPlan.prefSitesMatched());
    }

    @Test
    void generatePlansCombinesMultipleSitesWhenOneSiteIsNotEnough() throws SQLException {
        List<PlanDTO> plans = service.generatePlans(
                List.of(new ItemDemand("M002", "Mouse", 10, "pcs")),
                Map.of("M002", new ItemConfig("SITE01", null, DeliveryMeans.AIR_DELIVERY)),
                Map.of("M002", List.of(
                        new SiteStockTransportDTO("SITE01", "Tokyo Site", "Japan", 6, "pcs", 7, 2),
                        new SiteStockTransportDTO("SITE02", "Seoul Site", "Korea", 5, "pcs", 5, 1)
                ))
        );

        assertFalse(plans.isEmpty());
        PlanDTO plan = plans.get(0);
        int allocatedQuantity = plan.orders().stream()
                .flatMap(order -> order.items().stream())
                .mapToInt(AllocatedItem::quantity)
                .sum();

        assertEquals(10, allocatedQuantity);
        assertEquals(2, plan.uniqueSitesCount());
    }

    @Test
    void generatePlansCreatesDeliveryAlternativesWhenSiteSupportsShipAndAir() throws SQLException {
        List<PlanDTO> plans = service.generatePlans(
                List.of(new ItemDemand("M003", "Monitor", 4, "pcs")),
                Map.of("M003", new ItemConfig("SITE01", null, DeliveryMeans.AIR_DELIVERY)),
                Map.of("M003", List.of(
                        new SiteStockTransportDTO("SITE01", "Tokyo Site", "Japan", 10, "pcs", 7, 2)
                ))
        );

        assertEquals(2, plans.size());
        assertTrue(plans.stream().anyMatch(plan ->
                plan.orders().get(0).deliveryMeans() == DeliveryMeans.SHIP_DELIVERY));
        assertTrue(plans.stream().anyMatch(plan ->
                plan.orders().get(0).deliveryMeans() == DeliveryMeans.AIR_DELIVERY));
    }

    @Test
    void generatePlansDefaultsToAirDeliveryAndPrioritizesIt() throws SQLException {
        List<PlanDTO> plans = service.generatePlans(
                List.of(new ItemDemand("M001", "Keyboard", 10, "pcs")),
                Map.of(), // No configurations passed, should default to AIR_DELIVERY
                Map.of("M001", List.of(
                        new SiteStockTransportDTO("SITE01", "Tokyo Site", "Japan", 15, "pcs", 7, 2)
                ))
        );

        // Since it defaults to AIR_DELIVERY preference, the plan with AIR_DELIVERY (index 0 after sort) should be first
        assertFalse(plans.isEmpty());
        PlanDTO bestPlan = plans.get(0);
        assertEquals(DeliveryMeans.AIR_DELIVERY, bestPlan.orders().get(0).deliveryMeans());
        
        // Verify expected delivery date is set (should be now + 2 days for air)
        java.time.LocalDate expectedDate = bestPlan.orders().get(0).expectedDeliveryDate();
        assertEquals(java.time.LocalDate.now().plusDays(2), expectedDate);
    }

    private static class FakeImportSiteDao implements IImportSiteDao {
        private final List<ImportSite> sites = List.of(
                new ImportSite("SITE01", "Tokyo Site", "Japan", "tokyo@example.com"),
                new ImportSite("SITE02", "Seoul Site", "Korea", "seoul@example.com")
        );

        @Override
        public Optional<ImportSite> findById(String siteCode) {
            return sites.stream()
                    .filter(site -> site.getSiteCode().equals(siteCode))
                    .findFirst();
        }

        @Override
        public List<ImportSite> findAll() {
            return sites;
        }

        @Override
        public int insert(ImportSite importSite) {
            return 1;
        }

        @Override
        public boolean update(ImportSite importSite) {
            return true;
        }

        @Override
        public boolean delete(String siteCode) {
            return true;
        }
    }
}
