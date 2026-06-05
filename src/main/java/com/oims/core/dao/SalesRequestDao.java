package com.oims.core.dao;

import com.oims.core.model.SalesRequest;
import com.oims.core.model.SalesRequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesRequestDao extends BaseJdbcDao implements ISalesRequestDao {
    public Optional<SalesRequest> findById(int requestId) throws SQLException {
        String sql = "SELECT * FROM SalesRequest WHERE request_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<SalesRequest> findAll() throws SQLException {
        String sql = "SELECT * FROM SalesRequest ORDER BY request_id";
        List<SalesRequest> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public List<SalesRequest> findByCreatedBy(int createdBy) throws SQLException {
        String sql = "SELECT * FROM SalesRequest WHERE created_by = ? ORDER BY request_id";
        List<SalesRequest> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, createdBy);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(map(resultSet));
                }
            }
        }
        return results;
    }

    public int insert(SalesRequest salesRequest) throws SQLException {
        String sql = "INSERT INTO SalesRequest (created_by, created_date, status) VALUES (?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, salesRequest.getCreatedBy());
            statement.setDate(2, toDate(salesRequest.getCreatedDate()));
            statement.setString(3, salesRequest.getStatus().getDbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    salesRequest.setRequestId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean update(SalesRequest salesRequest) throws SQLException {
        String sql = "UPDATE SalesRequest SET created_by = ?, created_date = ?, status = ? WHERE request_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, salesRequest.getCreatedBy());
            statement.setDate(2, toDate(salesRequest.getCreatedDate()));
            statement.setString(3, salesRequest.getStatus().getDbValue());
            statement.setInt(4, salesRequest.getRequestId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int requestId) throws SQLException {
        String sql = "DELETE FROM SalesRequest WHERE request_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Connection connection, int requestId, SalesRequestStatus status) throws SQLException {
        String sql = "UPDATE SalesRequest SET status = ? WHERE request_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.getDbValue());
            statement.setInt(2, requestId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int requestId, SalesRequestStatus status) throws SQLException {
        try (Connection connection = connection()) {
            return updateStatus(connection, requestId, status);
        }
    }

    public boolean updateStatusIfCurrent(int requestId, SalesRequestStatus expectedStatus, SalesRequestStatus newStatus) throws SQLException {
        String sql = "UPDATE SalesRequest SET status = ? WHERE request_id = ? AND status = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus.getDbValue());
            statement.setInt(2, requestId);
            statement.setString(3, expectedStatus.getDbValue());
            return statement.executeUpdate() > 0;
        }
    }

    private SalesRequest map(ResultSet resultSet) throws SQLException {
        return new SalesRequest(
                resultSet.getInt("request_id"),
                resultSet.getInt("created_by"),
                toLocalDate(resultSet.getDate("created_date")),
                SalesRequestStatus.fromDbValue(resultSet.getString("status"))
        );
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection();
    }
}