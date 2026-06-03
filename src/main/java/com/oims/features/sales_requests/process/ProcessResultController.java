package com.oims.features.sales_requests.process;

import com.oims.core.dao.*;
import com.oims.core.model.*;

import java.sql.SQLException;
import java.util.*;

public class ProcessResultController {
    private final ISalesRequestDao salesRequestDao;
    private final IUserDao userDao;
    private final IPurchaseOrderDao purchaseOrderDao;
    private final IPurchaseOrderItemDao purchaseOrderItemDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final IMerchandiseDao merchandiseDao;
    private final IImportSiteDao importSiteDao;
    private final ISiteMerchandiseDao siteMerchandiseDao;

    public ProcessResultController() {
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
        this.userDao = DaoFactory.getUserDao();
        this.purchaseOrderDao = DaoFactory.getPurchaseOrderDao();
        this.purchaseOrderItemDao = DaoFactory.getPurchaseOrderItemDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.siteMerchandiseDao = DaoFactory.getSiteMerchandiseDao();
    }

    public ProcessResultController(ISalesRequestDao salesRequestDao, IUserDao userDao, IPurchaseOrderDao purchaseOrderDao,
                                   IPurchaseOrderItemDao purchaseOrderItemDao, ISalesRequestItemDao salesRequestItemDao,
                                   IMerchandiseDao merchandiseDao, IImportSiteDao importSiteDao, ISiteMerchandiseDao siteMerchandiseDao) {
        this.salesRequestDao = salesRequestDao;
        this.userDao = userDao;
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.merchandiseDao = merchandiseDao;
        this.importSiteDao = importSiteDao;
        this.siteMerchandiseDao = siteMerchandiseDao;
    }

    public Optional<SalesRequest> getSalesRequest(int requestId) throws SQLException {
        return salesRequestDao.findById(requestId);
    }

    public String getCreatorName(int userId) {
        try {
            return userDao.findById(userId)
                    .map(user -> {
                        String fullName = user.getFullName();
                        if (fullName != null && !fullName.isBlank()) return fullName;
                        return user.getUsername() == null || user.getUsername().isBlank() ? "Không xác định" : user.getUsername();
                    })
                    .orElse("Người dùng #" + userId);
        } catch (SQLException e) {
            return "Người dùng #" + userId;
        }
    }

    public List<PurchaseOrder> getPurchaseOrders(int requestId) throws SQLException {
        return purchaseOrderDao.findByRequestId(requestId);
    }

    public List<PurchaseOrderItem> getPurchaseOrderItems(int orderId) throws SQLException {
        return purchaseOrderItemDao.findByOrderId(orderId);
    }

    public List<SalesRequestItem> getSalesRequestItems(int requestId) throws SQLException {
        return salesRequestItemDao.findByRequestId(requestId);
    }

    public String resolveMerchandiseName(String merchandiseCode) {
        try {
            return merchandiseDao.findById(merchandiseCode)
                    .map(Merchandise::getMerchandiseName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    public String resolveSiteName(String siteCode) {
        try {
            return importSiteDao.findById(siteCode)
                    .map(ImportSite::getSiteName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    public List<ProcessedErrorDTO> getProcessingErrors(int requestId) throws SQLException {
        List<SalesRequestItem> requestItems = getSalesRequestItems(requestId);
        List<PurchaseOrder> purchaseOrders = getPurchaseOrders(requestId);

        Map<String, Integer> allocatedMap = new HashMap<>();
        for (PurchaseOrder po : purchaseOrders) {
            List<PurchaseOrderItem> poItems = getPurchaseOrderItems(po.getOrderId());
            for (PurchaseOrderItem poi : poItems) {
                allocatedMap.put(
                        poi.getMerchandiseCode(),
                        allocatedMap.getOrDefault(poi.getMerchandiseCode(), 0) + poi.getQuantityOrdered()
                );
            }
        }

        List<ProcessedErrorDTO> errors = new ArrayList<>();
        for (SalesRequestItem item : requestItems) {
            String code = item.getMerchandiseCode();
            int requested = item.getQuantityOrdered();
            int allocated = allocatedMap.getOrDefault(code, 0);

            if (allocated < requested) {
                String name = resolveMerchandiseName(code);
                int missing = requested - allocated;
                
                // Determine reason by checking partner stocks
                List<SiteMerchandise> siteStocks = siteMerchandiseDao.findByMerchandiseCode(code);
                String reason;
                if (siteStocks.isEmpty()) {
                    reason = "Không có site đối tác nào cung cấp mặt hàng này.";
                } else {
                    int totalStock = siteStocks.stream().mapToInt(SiteMerchandise::getInStockQuantity).sum();
                    if (totalStock < requested) {
                        reason = "Không đủ tồn kho khả dụng tại các site đối tác (Tổng tồn kho: " + totalStock + " " + item.getUnit() + ").";
                    } else {
                        reason = "Lỗi cấu hình hoặc không tìm thấy phương thức vận chuyển phù hợp.";
                    }
                }

                errors.add(new ProcessedErrorDTO(code, name, requested, allocated, missing, item.getUnit(), reason));
            }
        }
        return errors;
    }
}
