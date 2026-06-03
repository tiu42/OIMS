package com.oims.core.dao;

public class DaoFactory {
    private static IMerchandiseDao merchandiseDao = new MerchandiseDao();
    private static ISalesRequestDao salesRequestDao = new SalesRequestDao();
    private static ISalesRequestItemDao salesRequestItemDao = new SalesRequestItemDao();
    private static IUserDao userDao = new UserDao();
    private static IPurchaseOrderDao purchaseOrderDao = new PurchaseOrderDao();
    private static IPurchaseOrderItemDao purchaseOrderItemDao = new PurchaseOrderItemDao();
    private static ISiteMerchandiseDao siteMerchandiseDao = new SiteMerchandiseDao();
    private static ISiteTransportInfoDao siteTransportInfoDao = new SiteTransportInfoDao();
    private static IImportSiteDao importSiteDao = new ImportSiteDao();

    public static IMerchandiseDao getMerchandiseDao() {
        return merchandiseDao;
    }

    public static void setMerchandiseDao(IMerchandiseDao dao) {
        merchandiseDao = dao;
    }

    public static ISalesRequestDao getSalesRequestDao() {
        return salesRequestDao;
    }

    public static void setSalesRequestDao(ISalesRequestDao dao) {
        salesRequestDao = dao;
    }

    public static ISalesRequestItemDao getSalesRequestItemDao() {
        return salesRequestItemDao;
    }

    public static void setSalesRequestItemDao(ISalesRequestItemDao dao) {
        salesRequestItemDao = dao;
    }

    public static IUserDao getUserDao() {
        return userDao;
    }

    public static void setUserDao(IUserDao dao) {
        userDao = dao;
    }

    public static IPurchaseOrderDao getPurchaseOrderDao() {
        return purchaseOrderDao;
    }

    public static void setPurchaseOrderDao(IPurchaseOrderDao dao) {
        purchaseOrderDao = dao;
    }

    public static IPurchaseOrderItemDao getPurchaseOrderItemDao() {
        return purchaseOrderItemDao;
    }

    public static void setPurchaseOrderItemDao(IPurchaseOrderItemDao dao) {
        purchaseOrderItemDao = dao;
    }

    public static ISiteMerchandiseDao getSiteMerchandiseDao() {
        return siteMerchandiseDao;
    }

    public static void setSiteMerchandiseDao(ISiteMerchandiseDao dao) {
        siteMerchandiseDao = dao;
    }

    public static ISiteTransportInfoDao getSiteTransportInfoDao() {
        return siteTransportInfoDao;
    }

    public static void setSiteTransportInfoDao(ISiteTransportInfoDao dao) {
        siteTransportInfoDao = dao;
    }

    public static IImportSiteDao getImportSiteDao() {
        return importSiteDao;
    }

    public static void setImportSiteDao(IImportSiteDao dao) {
        importSiteDao = dao;
    }
}
