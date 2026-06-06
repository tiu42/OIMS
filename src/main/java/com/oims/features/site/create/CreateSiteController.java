package com.oims.features.site.create;

import com.oims.core.dao.DaoFactory;
import com.oims.core.dao.IImportSiteDao;
import com.oims.core.model.ImportSite;

import java.sql.SQLException;
import java.util.Optional;

public class CreateSiteController {
    private final IImportSiteDao importSiteDao;

    public CreateSiteController() {
        this.importSiteDao = DaoFactory.getImportSiteDao();
    }

    public CreateSiteController(IImportSiteDao importSiteDao) {
        this.importSiteDao = importSiteDao;
    }

    public Optional<String> validate(String siteCode, String siteName, String country, String contactInfo) throws SQLException {
        if (siteCode == null || siteCode.isBlank()) {
            return Optional.of("Mã site không được để trống.");
        }

        String normalizedCode = siteCode.trim().toUpperCase();
        if (normalizedCode.length() > 10) {
            return Optional.of("Mã site tối đa 10 ký tự.");
        }

        if (siteName == null || siteName.isBlank()) {
            return Optional.of("Tên site không được để trống.");
        }

        if (country == null || country.isBlank()) {
            return Optional.of("Quốc gia không được để trống.");
        }

        if (importSiteDao.findById(normalizedCode).isPresent()) {
            return Optional.of("Mã site \"" + normalizedCode + "\" đã tồn tại.");
        }

        return Optional.empty();
    }

    public String createSite(String siteCode, String siteName, String country, String contactInfo) throws SQLException {
        Optional<String> validationError = validate(siteCode, siteName, country, contactInfo);
        if (validationError.isPresent()) {
            return validationError.get();
        }

        String normalizedCode = siteCode.trim().toUpperCase();
        ImportSite site = new ImportSite(
                normalizedCode,
                siteName.trim(),
                country.trim(),
                contactInfo == null ? "" : contactInfo.trim()
        );

        int inserted = importSiteDao.insert(site);
        if (inserted <= 0) {
            return "Không thể thêm site đối tác.";
        }

        return null;
    }
}
