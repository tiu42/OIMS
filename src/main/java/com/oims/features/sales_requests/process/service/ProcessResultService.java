package com.oims.features.sales_requests.process.service;

import com.oims.core.dao.*;
import com.oims.core.model.*;
import com.oims.features.sales_requests.process.dto.ProcessedErrorDTO;
import com.oims.features.sales_requests.process.dto.SalesRequestDTO;
import com.oims.features.sales_requests.process.dto.PurchaseOrderDTO;
import com.oims.features.sales_requests.process.dto.AllocatedItem;

import java.sql.SQLException;
import java.util.*;

public class ProcessResultService implements IProcessResultService {
    private final IUserDao userDao;
    private final IPurchaseOrderDao purchaseOrderDao;
    private final IPurchaseOrderItemDao purchaseOrderItemDao;
    private final ISalesRequestItemDao salesRequestItemDao;
    private final IMerchandiseDao merchandiseDao;
    private final IImportSiteDao importSiteDao;
    private final ISiteMerchandiseDao siteMerchandiseDao;
    private final ISalesRequestDao salesRequestDao;

    public ProcessResultService() {
        this.userDao = DaoFactory.getUserDao();
        this.purchaseOrderDao = DaoFactory.getPurchaseOrderDao();
        this.purchaseOrderItemDao = DaoFactory.getPurchaseOrderItemDao();
        this.salesRequestItemDao = DaoFactory.getSalesRequestItemDao();
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.siteMerchandiseDao = DaoFactory.getSiteMerchandiseDao();
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
    }

    public ProcessResultService(IUserDao userDao, IPurchaseOrderDao purchaseOrderDao,
                                IPurchaseOrderItemDao purchaseOrderItemDao, ISalesRequestItemDao salesRequestItemDao,
                                IMerchandiseDao merchandiseDao, IImportSiteDao importSiteDao, ISiteMerchandiseDao siteMerchandiseDao,
                                ISalesRequestDao salesRequestDao) {
        this.userDao = userDao;
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.salesRequestItemDao = salesRequestItemDao;
        this.merchandiseDao = merchandiseDao;
        this.importSiteDao = importSiteDao;
        this.siteMerchandiseDao = siteMerchandiseDao;
        this.salesRequestDao = salesRequestDao;
    }

    @Override
    public Optional<SalesRequestDTO> getSalesRequest(int requestId) throws SQLException {
        return salesRequestDao.findById(requestId)
                .map(req -> {
                    String statusText = switch (req.getStatus()) {
                        case PENDING -> "Chờ xử lý";
                        case PROCESSING -> "Đang xử lý";
                        case COMPLETED -> "Hoàn tất";
                        case ERROR -> "Lỗi";
                    };
                    return new SalesRequestDTO(
                            req.getRequestId(),
                            req.getCreatedBy(),
                            req.getCreatedDate(),
                            statusText
                    );
                });
    }

    @Override
    public List<PurchaseOrderDTO> getPurchaseOrders(int requestId) throws SQLException {
        List<PurchaseOrder> pos = purchaseOrderDao.findByRequestId(requestId);
        List<PurchaseOrderDTO> dtos = new ArrayList<>();
        for (PurchaseOrder po : pos) {
            String siteName = resolveSiteName(po.getSiteCode());
            dtos.add(new PurchaseOrderDTO(
                    po.getOrderId(),
                    po.getRequestId(),
                    po.getSiteCode(),
                    siteName,
                    po.getCreatedBy(),
                    po.getOrderDate(),
                    po.getDeliveryMeans(),
                    po.getStatus()
            ));
        }
        return dtos;
    }

    @Override
    public List<AllocatedItem> getPurchaseOrderItems(int orderId) throws SQLException {
        List<PurchaseOrderItem> items = purchaseOrderItemDao.findByOrderId(orderId);
        List<AllocatedItem> dtos = new ArrayList<>();
        for (PurchaseOrderItem item : items) {
            String merchName = resolveMerchandiseName(item.getMerchandiseCode());
            dtos.add(new AllocatedItem(
                    item.getMerchandiseCode(),
                    merchName,
                    item.getQuantityOrdered(),
                    item.getUnit()
            ));
        }
        return dtos;
    }

    @Override
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

    @Override
    public String resolveMerchandiseName(String merchandiseCode) {
        try {
            return merchandiseDao.findById(merchandiseCode)
                    .map(Merchandise::getMerchandiseName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    @Override
    public String resolveSiteName(String siteCode) {
        try {
            return importSiteDao.findById(siteCode)
                    .map(ImportSite::getSiteName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    @Override
    public List<ProcessedErrorDTO> getProcessingErrors(int requestId) throws SQLException {
        List<SalesRequestItem> requestItems = salesRequestItemDao.findByRequestId(requestId);
        List<PurchaseOrder> purchaseOrders = purchaseOrderDao.findByRequestId(requestId);

        Map<String, Integer> allocatedMap = new HashMap<>();
        for (PurchaseOrder po : purchaseOrders) {
            List<PurchaseOrderItem> poItems = purchaseOrderItemDao.findByOrderId(po.getOrderId());
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
