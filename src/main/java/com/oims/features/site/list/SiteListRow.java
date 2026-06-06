package com.oims.features.site.list;

import com.oims.core.model.ImportSite;

import java.util.Locale;

public final class SiteListRow {
    private final ImportSite site;

    public SiteListRow(ImportSite site) {
        this.site = site;
    }

    public String getSiteCode() {
        return site.getSiteCode();
    }

    public String getSiteName() {
        return site.getSiteName() == null ? "" : site.getSiteName();
    }

    public String getCountry() {
        return site.getCountry() == null ? "" : site.getCountry();
    }

    public String getContactInfo() {
        return site.getContactInfo() == null ? "" : site.getContactInfo();
    }

    public String getSearchText() {
        return (getSiteCode() + " " + getSiteName() + " " + getCountry() + " " + getContactInfo())
                .toLowerCase(Locale.ROOT);
    }
}
