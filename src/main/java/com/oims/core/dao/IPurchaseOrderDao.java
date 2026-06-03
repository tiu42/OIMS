package com.oims.core.dao;

import com.oims.core.model.PurchaseOrder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IPurchaseOrderDao {
    Optional<PurchaseOrder> findById(int orderId) throws SQLException;
    List<PurchaseOrder> findAll() throws SQLException;
    List<PurchaseOrder> findByRequestId(int requestId) throws SQLException;
    List<PurchaseOrder> findByCreatedBy(int createdBy) throws SQLException;
    int insert(PurchaseOrder purchaseOrder) throws SQLException;
    int insert(Connection connection, PurchaseOrder purchaseOrder) throws SQLException;
    boolean update(PurchaseOrder purchaseOrder) throws SQLException;
    boolean delete(int orderId) throws SQLException;
}
