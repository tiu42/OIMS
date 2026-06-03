package com.oims.core.dao;

import com.oims.core.model.SiteMerchandise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiteMerchandiseDao extends BaseJdbcDao implements ISiteMerchandiseDao {
    public Optional<SiteMerchandise> findById(String siteCode, String merchandiseCode) throws SQLException {
        String sql = "SELECT * FROM SiteMerchandise WHERE site_code = ? AND merchandise_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteCode);
            statement.setString(2, merchandiseCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<SiteMerchandise> findAll() throws SQLException {
        String sql = "SELECT * FROM SiteMerchandise ORDER BY site_code, merchandise_code";
        List<SiteMerchandise> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public List<SiteMerchandise> findBySiteCode(String siteCode) throws SQLException {
        String sql = "SELECT * FROM SiteMerchandise WHERE site_code = ? ORDER BY merchandise_code";
        List<SiteMerchandise> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(map(resultSet));
                }
            }
        }
        return results;
    }

    public List<SiteMerchandise> findByMerchandiseCode(String merchandiseCode) throws SQLException {
        String sql = "SELECT * FROM SiteMerchandise WHERE merchandise_code = ? ORDER BY site_code";
        List<SiteMerchandise> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, merchandiseCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(map(resultSet));
                }
            }
        }
        return results;
    }

    public int insert(SiteMerchandise siteMerchandise) throws SQLException {
        String sql = "INSERT INTO SiteMerchandise (site_code, merchandise_code, in_stock_quantity, unit, stock_updated_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteMerchandise.getSiteCode());
            statement.setString(2, siteMerchandise.getMerchandiseCode());
            statement.setInt(3, siteMerchandise.getInStockQuantity());
            statement.setString(4, siteMerchandise.getUnit());
            statement.setDate(5, toDate(siteMerchandise.getStockUpdatedDate()));
            return statement.executeUpdate();
        }
    }

    public boolean update(SiteMerchandise siteMerchandise) throws SQLException {
        String sql = "UPDATE SiteMerchandise SET in_stock_quantity = ?, unit = ?, stock_updated_date = ? WHERE site_code = ? AND merchandise_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, siteMerchandise.getInStockQuantity());
            statement.setString(2, siteMerchandise.getUnit());
            statement.setDate(3, toDate(siteMerchandise.getStockUpdatedDate()));
            statement.setString(4, siteMerchandise.getSiteCode());
            statement.setString(5, siteMerchandise.getMerchandiseCode());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(String siteCode, String merchandiseCode) throws SQLException {
        String sql = "DELETE FROM SiteMerchandise WHERE site_code = ? AND merchandise_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteCode);
            statement.setString(2, merchandiseCode);
            return statement.executeUpdate() > 0;
        }
    }

    private SiteMerchandise map(ResultSet resultSet) throws SQLException {
        return new SiteMerchandise(
                resultSet.getString("site_code"),
                resultSet.getString("merchandise_code"),
                resultSet.getInt("in_stock_quantity"),
                resultSet.getString("unit"),
                toLocalDate(resultSet.getDate("stock_updated_date"))
        );
    }
}