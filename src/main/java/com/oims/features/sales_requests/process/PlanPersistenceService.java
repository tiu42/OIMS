package com.oims.features.sales_requests.process;

import com.oims.core.database.DBConnection;
import com.oims.core.dao.IPurchaseOrderDao;
import com.oims.core.dao.IPurchaseOrderItemDao;
import com.oims.core.dao.ISalesRequestDao;
import com.oims.core.dao.DaoFactory;
import com.oims.core.model.PurchaseOrder;
import com.oims.core.model.PurchaseOrderItem;
import com.oims.core.model.PurchaseOrderStatus;
import com.oims.core.model.SalesRequestStatus;
import com.oims.core.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class PlanPersistenceService implements IPlanPersistenceService {
    private final IPurchaseOrderDao purchaseOrderDao;
    private final IPurchaseOrderItemDao purchaseOrderItemDao;
    private final ISalesRequestDao salesRequestDao;

    public PlanPersistenceService() {
        this.purchaseOrderDao = DaoFactory.getPurchaseOrderDao();
        this.purchaseOrderItemDao = DaoFactory.getPurchaseOrderItemDao();
        this.salesRequestDao = DaoFactory.getSalesRequestDao();
    }

    public PlanPersistenceService(IPurchaseOrderDao purchaseOrderDao, IPurchaseOrderItemDao purchaseOrderItemDao, ISalesRequestDao salesRequestDao) {
        this.purchaseOrderDao = purchaseOrderDao;
        this.purchaseOrderItemDao = purchaseOrderItemDao;
        this.salesRequestDao = salesRequestDao;
    }

    public void savePlan(int requestId, User creator, PlanDTO plan, boolean hasErrors) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create Purchase Orders
                for (AllocatedOrder order : plan.orders()) {
                    PurchaseOrder po = new PurchaseOrder(
                            0,
                            requestId,
                            order.siteCode(),
                            creator.getUserId(),
                            LocalDate.now(),
                            order.deliveryMeans(),
                            PurchaseOrderStatus.DRAFT
                    );

                    int orderId = purchaseOrderDao.insert(conn, po);
                    if (orderId <= 0) {
                        throw new SQLException("Không thể tạo đơn hàng PurchaseOrder.");
                    }
                    
                    // 2. Create Purchase Order Items
                    for (AllocatedItem item : order.items()) {
                        PurchaseOrderItem poItem = new PurchaseOrderItem(
                                0,
                                orderId,
                                item.merchandiseCode(),
                                item.quantity(),
                                item.unit()
                        );
                        purchaseOrderItemDao.insert(conn, poItem);
                    }
                }
                
                // 3. Update Sales Request status
                SalesRequestStatus targetStatus = hasErrors ? SalesRequestStatus.ERROR : SalesRequestStatus.COMPLETED;
                boolean updated = salesRequestDao.updateStatus(conn, requestId, targetStatus);
                if (!updated) {
                    throw new SQLException("Không thể cập nhật trạng thái yêu cầu SalesRequest.");
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
