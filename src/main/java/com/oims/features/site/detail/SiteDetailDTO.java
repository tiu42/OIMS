package com.oims.features.site.detail;

public final class SiteDetailDTO {
    private final String siteCode;
    private final String siteName;
    private final String country;
    private final String contactInfo;

    public SiteDetailDTO(String siteCode, String siteName, String country, String contactInfo) {
        this.siteCode = siteCode;
        this.siteName = siteName;
        this.country = country;
        this.contactInfo = contactInfo;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getCountry() {
        return country;
    }

    public String getContactInfo() {
        return contactInfo;
    }
}
