package com.oims.core.dao;

import com.oims.core.model.DeliveryMeans;
import com.oims.core.model.PurchaseOrder;
import com.oims.core.model.PurchaseOrderStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderDao extends BaseJdbcDao implements IPurchaseOrderDao {
    public Optional<PurchaseOrder> findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM PurchaseOrder WHERE order_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<PurchaseOrder> findAll() throws SQLException {
        String sql = "SELECT * FROM PurchaseOrder ORDER BY order_id";
        List<PurchaseOrder> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public List<PurchaseOrder> findByRequestId(int requestId) throws SQLException {
        String sql = "SELECT * FROM PurchaseOrder WHERE request_id = ? ORDER BY order_id";
        List<PurchaseOrder> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(map(resultSet));
                }
            }
        }
        return results;
    }

    public List<PurchaseOrder> findByCreatedBy(int createdBy) throws SQLException {
        String sql = "SELECT * FROM PurchaseOrder WHERE created_by = ? ORDER BY order_id";
        List<PurchaseOrder> results = new ArrayList<>();
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

    public int insert(PurchaseOrder purchaseOrder) throws SQLException {
        try (Connection connection = connection()) {
            return insert(connection, purchaseOrder);
        }
    }

    public int insert(Connection connection, PurchaseOrder purchaseOrder) throws SQLException {
        String sql = "INSERT INTO PurchaseOrder (request_id, site_code, created_by, order_date, delivery_means, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, purchaseOrder.getRequestId());
            statement.setString(2, purchaseOrder.getSiteCode());
            statement.setInt(3, purchaseOrder.getCreatedBy());
            statement.setDate(4, toDate(purchaseOrder.getOrderDate()));
            statement.setString(5, purchaseOrder.getDeliveryMeans().getDbValue());
            statement.setString(6, purchaseOrder.getStatus().getDbValue());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    purchaseOrder.setOrderId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean update(PurchaseOrder purchaseOrder) throws SQLException {
        String sql = "UPDATE PurchaseOrder SET request_id = ?, site_code = ?, created_by = ?, order_date = ?, delivery_means = ?, status = ? WHERE order_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchaseOrder.getRequestId());
            statement.setString(2, purchaseOrder.getSiteCode());
            statement.setInt(3, purchaseOrder.getCreatedBy());
            statement.setDate(4, toDate(purchaseOrder.getOrderDate()));
            statement.setString(5, purchaseOrder.getDeliveryMeans().getDbValue());
            statement.setString(6, purchaseOrder.getStatus().getDbValue());
            statement.setInt(7, purchaseOrder.getOrderId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int orderId) throws SQLException {
        String sql = "DELETE FROM PurchaseOrder WHERE order_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    private PurchaseOrder map(ResultSet resultSet) throws SQLException {
        return new PurchaseOrder(
                resultSet.getInt("order_id"),
                resultSet.getInt("request_id"),
                resultSet.getString("site_code"),
                resultSet.getInt("created_by"),
                toLocalDate(resultSet.getDate("order_date")),
                DeliveryMeans.fromDbValue(resultSet.getString("delivery_means")),
                PurchaseOrderStatus.fromDbValue(resultSet.getString("status"))
        );
    }
}