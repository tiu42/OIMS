package com.oims.features.site.detail;

import com.oims.core.dao.DaoFactory;
import com.oims.core.dao.IMerchandiseDao;
import com.oims.core.dao.IImportSiteDao;
import com.oims.core.dao.ISiteMerchandiseDao;
import com.oims.core.dao.ISiteTransportInfoDao;
import com.oims.core.model.ImportSite;
import com.oims.core.model.SiteMerchandise;
import com.oims.core.model.SiteTransportInfo;
import com.oims.core.session.AppSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiteDetailController {
    private final IImportSiteDao importSiteDao;
    private final ISiteTransportInfoDao transportInfoDao;
    private final ISiteMerchandiseDao siteMerchandiseDao;
    private final IMerchandiseDao merchandiseDao;

    public SiteDetailController() {
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.transportInfoDao = DaoFactory.getSiteTransportInfoDao();
        this.siteMerchandiseDao = DaoFactory.getSiteMerchandiseDao();
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
    }

    public SiteDetailController(
            IImportSiteDao importSiteDao,
            ISiteTransportInfoDao transportInfoDao,
            ISiteMerchandiseDao siteMerchandiseDao,
            IMerchandiseDao merchandiseDao
    ) {
        this.importSiteDao = importSiteDao;
        this.transportInfoDao = transportInfoDao;
        this.siteMerchandiseDao = siteMerchandiseDao;
        this.merchandiseDao = merchandiseDao;
    }

    public Optional<SiteDetailDTO> loadSiteData() throws SQLException {
        String siteCode = AppSession.getInstance().getSelectedSiteCode();
        if (siteCode == null || siteCode.isBlank()) {
            return Optional.empty();
        }

        Optional<ImportSite> siteOpt = importSiteDao.findById(siteCode);
        if (siteOpt.isEmpty()) {
            return Optional.empty();
        }

        ImportSite site = siteOpt.get();
        return Optional.of(new SiteDetailDTO(
                site.getSiteCode(),
                site.getSiteName() == null ? "" : site.getSiteName(),
                site.getCountry() == null ? "" : site.getCountry(),
                site.getContactInfo() == null ? "" : site.getContactInfo()
        ));
    }

    public List<SiteTransportRow> loadTransportData() throws SQLException {
        String siteCode = AppSession.getInstance().getSelectedSiteCode();
        if (siteCode == null || siteCode.isBlank()) {
            return List.of();
        }

        List<SiteTransportRow> rows = new ArrayList<>();
        for (SiteTransportInfo info : transportInfoDao.findBySiteCode(siteCode)) {
            rows.add(new SiteTransportRow(info));
        }
        return rows;
    }

    public List<SiteMerchandiseRow> loadMerchandiseData() throws SQLException {
        String siteCode = AppSession.getInstance().getSelectedSiteCode();
        if (siteCode == null || siteCode.isBlank()) {
            return List.of();
        }

        List<SiteMerchandiseRow> rows = new ArrayList<>();
        for (SiteMerchandise stock : siteMerchandiseDao.findBySiteCode(siteCode)) {
            String merchandiseName = merchandiseDao.findById(stock.getMerchandiseCode())
                    .map(merchandise -> merchandise.getMerchandiseName())
                    .orElse("");
            rows.add(new SiteMerchandiseRow(stock.getMerchandiseCode(), merchandiseName, stock));
        }
        return rows;
    }
}
