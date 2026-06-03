package com.oims.core.dao;

import com.oims.core.model.PurchaseOrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderItemDao extends BaseJdbcDao implements IPurchaseOrderItemDao {
    public Optional<PurchaseOrderItem> findById(int orderItemId) throws SQLException {
        String sql = "SELECT * FROM PurchaseOrderItem WHERE order_item_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderItemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<PurchaseOrderItem> findAll() throws SQLException {
        String sql = "SELECT * FROM PurchaseOrderItem ORDER BY order_item_id";
        List<PurchaseOrderItem> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public List<PurchaseOrderItem> findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM PurchaseOrderItem WHERE order_id = ? ORDER BY order_item_id";
        List<PurchaseOrderItem> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(map(resultSet));
                }
            }
        }
        return results;
    }

    public int insert(PurchaseOrderItem item) throws SQLException {
        try (Connection connection = connection()) {
            return insert(connection, item);
        }
    }

    public int insert(Connection connection, PurchaseOrderItem item) throws SQLException {
        String sql = "INSERT INTO PurchaseOrderItem (order_id, merchandise_code, quantity_ordered, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, item.getOrderId());
            statement.setString(2, item.getMerchandiseCode());
            statement.setInt(3, item.getQuantityOrdered());
            statement.setString(4, item.getUnit());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    item.setOrderItemId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean update(PurchaseOrderItem item) throws SQLException {
        String sql = "UPDATE PurchaseOrderItem SET order_id = ?, merchandise_code = ?, quantity_ordered = ?, unit = ? WHERE order_item_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getOrderId());
            statement.setString(2, item.getMerchandiseCode());
            statement.setInt(3, item.getQuantityOrdered());
            statement.setString(4, item.getUnit());
            statement.setInt(5, item.getOrderItemId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int orderItemId) throws SQLException {
        String sql = "DELETE FROM PurchaseOrderItem WHERE order_item_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderItemId);
            return statement.executeUpdate() > 0;
        }
    }

    private PurchaseOrderItem map(ResultSet resultSet) throws SQLException {
        return new PurchaseOrderItem(
                resultSet.getInt("order_item_id"),
                resultSet.getInt("order_id"),
                resultSet.getString("merchandise_code"),
                resultSet.getInt("quantity_ordered"),
                resultSet.getString("unit")
        );
    }
}