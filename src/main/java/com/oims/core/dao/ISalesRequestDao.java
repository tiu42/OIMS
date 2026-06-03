package com.oims.core.dao;

import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ISalesRequestDao {
    Optional<SalesRequest> findById(int requestId) throws SQLException;
    List<SalesRequest> findAll() throws SQLException;
    List<SalesRequest> findByCreatedBy(int createdBy) throws SQLException;
    int insert(SalesRequest salesRequest) throws SQLException;
    boolean update(SalesRequest salesRequest) throws SQLException;
    boolean delete(int requestId) throws SQLException;
    boolean updateStatus(Connection connection, int requestId, SalesRequestStatus status) throws SQLException;
}
