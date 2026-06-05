package com.oims.core.dao;

import com.oims.core.model.SalesRequestItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ISalesRequestItemDao {
    Optional<SalesRequestItem> findById(int itemId) throws SQLException;
    List<SalesRequestItem> findAll() throws SQLException;
    List<SalesRequestItem> findByRequestId(int requestId) throws SQLException;
    int insert(SalesRequestItem item) throws SQLException;
    int insert(Connection connection, SalesRequestItem item) throws SQLException;
    boolean update(SalesRequestItem item) throws SQLException;
    boolean delete(int itemId) throws SQLException;
    void deleteByRequestId(int requestId) throws SQLException;
    void deleteByRequestId(Connection connection, int requestId) throws SQLException;
}
