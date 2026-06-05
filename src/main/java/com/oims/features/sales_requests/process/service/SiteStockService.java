package com.oims.features.sales_requests.process.service;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.features.sales_requests.process.dto.SiteStockTransportDTO;
import com.oims.features.sales_requests.process.dto.ItemDemand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiteStockService implements ISiteStockService {
    private final ISiteMerchandiseDao siteMerchandiseDao;
    private final ISiteTransportInfoDao siteTransportInfoDao;
    private final IImportSiteDao importSiteDao;

    public SiteStockService() {
        this.siteMerchandiseDao = DaoFactory.getSiteMerchandiseDao();
        this.siteTransportInfoDao = DaoFactory.getSiteTransportInfoDao();
        this.importSiteDao = DaoFactory.getImportSiteDao();
    }

    public SiteStockService(ISiteMerchandiseDao siteMerchandiseDao,
                            ISiteTransportInfoDao siteTransportInfoDao,
                            IImportSiteDao importSiteDao) {
        this.siteMerchandiseDao = siteMerchandiseDao;
        this.siteTransportInfoDao = siteTransportInfoDao;
        this.importSiteDao = importSiteDao;
    }

    @Override
    public List<SiteStockTransportDTO> getSiteStockAndTransport(String merchandiseCode, String excludedSiteCode) throws SQLException {
        List<SiteStockTransportDTO> result = new ArrayList<>();
        List<SiteMerchandise> siteMerches = siteMerchandiseDao.findByMerchandiseCode(merchandiseCode);
        
        for (SiteMerchandise sm : siteMerches) {
            String siteCode = sm.getSiteCode();
            if (excludedSiteCode != null && siteCode.equals(excludedSiteCode)) {
                continue;
            }
            
            String siteName = "Không xác định";
            String country = "Không xác định";
            Optional<ImportSite> siteOpt = importSiteDao.findById(siteCode);
            if (siteOpt.isPresent()) {
                siteName = siteOpt.get().getSiteName();
                country = siteOpt.get().getCountry();
            }

            int shipDays = 0;
            int airDays = 0;
            List<SiteTransportInfo> transInfos = siteTransportInfoDao.findBySiteCode(siteCode);
            if (!transInfos.isEmpty()) {
                shipDays = transInfos.get(0).getShipDays();
                airDays = transInfos.get(0).getAirDays();
            }

            result.add(new SiteStockTransportDTO(
                    siteCode,
                    siteName,
                    country,
                    sm.getInStockQuantity(),
                    sm.getUnit(),
                    shipDays,
                    airDays
            ));
        }
        return result;
    }

    @Override
    public List<ItemDemand> getFailedDemands(List<ItemDemand> demands, String excludedSiteCode) throws SQLException {
        List<ItemDemand> failed = new ArrayList<>();
        for (ItemDemand demand : demands) {
            List<SiteStockTransportDTO> sites = getSiteStockAndTransport(demand.merchandiseCode(), excludedSiteCode);
            int totalStock = sites.stream().mapToInt(SiteStockTransportDTO::inStock).sum();
            if (sites.isEmpty() || totalStock < demand.quantity()) {
                failed.add(demand);
            }
        }
        return failed;
    }
}
