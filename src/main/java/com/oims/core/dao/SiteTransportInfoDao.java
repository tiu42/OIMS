package com.oims.core.dao;

import com.oims.core.model.SiteTransportInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SiteTransportInfoDao extends BaseJdbcDao implements ISiteTransportInfoDao {
    public Optional<SiteTransportInfo> findById(int transportId) throws SQLException {
        String sql = "SELECT * FROM SiteTransportInfo WHERE transport_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transportId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<SiteTransportInfo> findAll() throws SQLException {
        String sql = "SELECT * FROM SiteTransportInfo ORDER BY transport_id";
        List<SiteTransportInfo> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public List<SiteTransportInfo> findBySiteCode(String siteCode) throws SQLException {
        String sql = "SELECT * FROM SiteTransportInfo WHERE site_code = ? ORDER BY transport_id";
        List<SiteTransportInfo> results = new ArrayList<>();
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

    public int insert(SiteTransportInfo siteTransportInfo) throws SQLException {
        String sql = "INSERT INTO SiteTransportInfo (site_code, ship_days, air_days, other_info, updated_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, siteTransportInfo.getSiteCode());
            statement.setInt(2, siteTransportInfo.getShipDays());
            statement.setInt(3, siteTransportInfo.getAirDays());
            statement.setString(4, siteTransportInfo.getOtherInfo());
            statement.setDate(5, toDate(siteTransportInfo.getUpdatedDate()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    siteTransportInfo.setTransportId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean update(SiteTransportInfo siteTransportInfo) throws SQLException {
        String sql = "UPDATE SiteTransportInfo SET site_code = ?, ship_days = ?, air_days = ?, other_info = ?, updated_date = ? WHERE transport_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteTransportInfo.getSiteCode());
            statement.setInt(2, siteTransportInfo.getShipDays());
            statement.setInt(3, siteTransportInfo.getAirDays());
            statement.setString(4, siteTransportInfo.getOtherInfo());
            statement.setDate(5, toDate(siteTransportInfo.getUpdatedDate()));
            statement.setInt(6, siteTransportInfo.getTransportId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int transportId) throws SQLException {
        String sql = "DELETE FROM SiteTransportInfo WHERE transport_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transportId);
            return statement.executeUpdate() > 0;
        }
    }

    private SiteTransportInfo map(ResultSet resultSet) throws SQLException {
        return new SiteTransportInfo(
                resultSet.getInt("transport_id"),
                resultSet.getString("site_code"),
                resultSet.getInt("ship_days"),
                resultSet.getInt("air_days"),
                resultSet.getString("other_info"),
                toLocalDate(resultSet.getDate("updated_date"))
        );
    }
}