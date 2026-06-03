package com.oims.core.dao;

import com.oims.core.model.PurchaseOrderItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IPurchaseOrderItemDao {
    Optional<PurchaseOrderItem> findById(int orderItemId) throws SQLException;
    List<PurchaseOrderItem> findAll() throws SQLException;
    List<PurchaseOrderItem> findByOrderId(int orderId) throws SQLException;
    int insert(PurchaseOrderItem item) throws SQLException;
    int insert(Connection connection, PurchaseOrderItem item) throws SQLException;
    boolean update(PurchaseOrderItem item) throws SQLException;
    boolean delete(int orderItemId) throws SQLException;
}
