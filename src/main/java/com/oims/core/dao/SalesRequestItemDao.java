package com.oims.core.dao;

import com.oims.core.model.SalesRequestItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesRequestItemDao extends BaseJdbcDao implements ISalesRequestItemDao {
    public Optional<SalesRequestItem> findById(int itemId) throws SQLException {
        String sql = "SELECT * FROM SalesRequestItem WHERE item_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<SalesRequestItem> findAll() throws SQLException {
        String sql = "SELECT * FROM SalesRequestItem ORDER BY item_id";
        List<SalesRequestItem> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public List<SalesRequestItem> findByRequestId(int requestId) throws SQLException {
        String sql = "SELECT * FROM SalesRequestItem WHERE request_id = ? ORDER BY item_id";
        List<SalesRequestItem> results = new ArrayList<>();
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

    public int insert(SalesRequestItem item) throws SQLException {
        String sql = "INSERT INTO SalesRequestItem (request_id, merchandise_code, quantity_ordered, unit, desired_delivery_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, item.getRequestId());
            statement.setString(2, item.getMerchandiseCode());
            statement.setInt(3, item.getQuantityOrdered());
            statement.setString(4, item.getUnit());
            statement.setDate(5, toDate(item.getDesiredDeliveryDate()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    item.setItemId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean update(SalesRequestItem item) throws SQLException {
        String sql = "UPDATE SalesRequestItem SET request_id = ?, merchandise_code = ?, quantity_ordered = ?, unit = ?, desired_delivery_date = ? WHERE item_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, item.getRequestId());
            statement.setString(2, item.getMerchandiseCode());
            statement.setInt(3, item.getQuantityOrdered());
            statement.setString(4, item.getUnit());
            statement.setDate(5, toDate(item.getDesiredDeliveryDate()));
            statement.setInt(6, item.getItemId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int itemId) throws SQLException {
        String sql = "DELETE FROM SalesRequestItem WHERE item_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            return statement.executeUpdate() > 0;
        }
    }

    public void deleteByRequestId(int requestId) throws SQLException {
        String sql = "DELETE FROM SalesRequestItem WHERE request_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();
        }
    }

    private SalesRequestItem map(ResultSet resultSet) throws SQLException {
        return new SalesRequestItem(
                resultSet.getInt("item_id"),
                resultSet.getInt("request_id"),
                resultSet.getString("merchandise_code"),
                resultSet.getInt("quantity_ordered"),
                resultSet.getString("unit"),
                toLocalDate(resultSet.getDate("desired_delivery_date"))
        );
    }
}