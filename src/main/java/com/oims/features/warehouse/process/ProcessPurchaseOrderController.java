package com.oims.features.warehouse.process;

import com.oims.core.dao.*;
import com.oims.core.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProcessPurchaseOrderController {
    private final IPurchaseOrderDao purchaseOrderDao;
    private final IPurchaseOrderItemDao purchaseOrderItemDao;
    private final IUserDao userDao;
    private final IImportSiteDao importSiteDao;
    private final IMerchandiseDao merchandiseDao;

    public ProcessPurchaseOrderController() {
        this.purchaseOrderDao = DaoFactory.getPurchaseOrderDao();
        this.purchaseOrderItemDao = DaoFactory.getPurchaseOrderItemDao();
        this.userDao = DaoFactory.getUserDao();
        this.importSiteDao = DaoFactory.getImportSiteDao();
        this.merchandiseDao = DaoFactory.getMerchandiseDao();
    }

    public ProcessPurchaseOrderController(IPurchaseOrderDao purchaseOrderDao, IPurchaseOrderItemDao purchaseOrderItemDao,
                                          IUserDao userDao, IImportSiteDao importSiteDao, IMerchandiseDao merchandiseDao) {
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.userDao = userDao;
        this.importSiteDao = importSiteDao;
        this.merchandiseDao = merchandiseDao;
    }

    public Optional<PurchaseOrder> getPurchaseOrder(int orderId) throws SQLException {
        return purchaseOrderDao.findById(orderId);
    }

    public List<PurchaseOrderItem> getPurchaseOrderItems(int orderId) throws SQLException {
        return purchaseOrderItemDao.findByOrderId(orderId);
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

    public String resolveCreatorName(Integer userId) {
        if (userId == null) return "Không xác định";
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

    public String resolveMerchandiseName(String merchandiseCode) {
        try {
            return merchandiseDao.findById(merchandiseCode)
                    .map(Merchandise::getMerchandiseName)
                    .orElse("Không xác định");
        } catch (SQLException e) {
            return "Không xác định";
        }
    }

    public void validateAndApproveReceipt(int orderId, List<ItemShortageResult> items) throws SQLException, IllegalArgumentException {
        // Validation
        for (ItemShortageResult row : items) {
            if ("Thiếu".equals(row.status())) {
                if (row.shortageQty() <= 0) {
                    throw new IllegalArgumentException("Số lượng thiếu phải lớn hơn 0 cho mặt hàng: " + row.merchandiseName());
                }
                if (row.shortageQty() > row.quantityOrdered()) {
                    throw new IllegalArgumentException("Số lượng thiếu (" + row.shortageQty() + ") không được vượt quá số lượng đặt (" + row.quantityOrdered() + ") cho mặt hàng: " + row.merchandiseName());
                }
            }
        }

        Optional<PurchaseOrder> orderOpt = purchaseOrderDao.findById(orderId);
        if (orderOpt.isPresent()) {
            PurchaseOrder order = orderOpt.get();
            order.setStatus(PurchaseOrderStatus.DELIVERED);
            purchaseOrderDao.update(order);
        } else {
            throw new SQLException("Đơn hàng không tồn tại để duyệt.");
        }
    }

    public record ItemShortageResult(
            String merchandiseCode,
            String merchandiseName,
            String status,
            int quantityOrdered,
            int shortageQty
    ) {}
}
