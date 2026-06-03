package com.oims.core.dao;

import com.oims.core.model.Merchandise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MerchandiseDao extends BaseJdbcDao implements IMerchandiseDao {
    public Optional<Merchandise> findById(String merchandiseCode) throws SQLException {
        String sql = "SELECT * FROM Merchandise WHERE merchandise_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, merchandiseCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<Merchandise> findAll() throws SQLException {
        String sql = "SELECT * FROM Merchandise ORDER BY merchandise_code";
        List<Merchandise> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public int insert(Merchandise merchandise) throws SQLException {
        String sql = "INSERT INTO Merchandise (merchandise_code, merchandise_name, default_unit) VALUES (?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, merchandise.getMerchandiseCode());
            statement.setString(2, merchandise.getMerchandiseName());
            statement.setString(3, merchandise.getDefaultUnit());
            return statement.executeUpdate();
        }
    }

    public boolean update(Merchandise merchandise) throws SQLException {
        String sql = "UPDATE Merchandise SET merchandise_name = ?, default_unit = ? WHERE merchandise_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, merchandise.getMerchandiseName());
            statement.setString(2, merchandise.getDefaultUnit());
            statement.setString(3, merchandise.getMerchandiseCode());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(String merchandiseCode) throws SQLException {
        String sql = "DELETE FROM Merchandise WHERE merchandise_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, merchandiseCode);
            return statement.executeUpdate() > 0;
        }
    }

    private Merchandise map(ResultSet resultSet) throws SQLException {
        return new Merchandise(
                resultSet.getString("merchandise_code"),
                resultSet.getString("merchandise_name"),
                resultSet.getString("default_unit")
        );
    }
}