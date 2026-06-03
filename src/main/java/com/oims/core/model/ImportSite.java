package com.oims.core.model;

public class ImportSite {
    private String siteCode;
    private String siteName;
    private String country;
    private String contactInfo;

    public ImportSite() {
    }

    public ImportSite(String siteCode, String siteName, String country, String contactInfo) {
        this.siteCode = siteCode;
        this.siteName = siteName;
        this.country = country;
        this.contactInfo = contactInfo;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}