package com.oims.core.dao;

import com.oims.core.model.ImportSite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportSiteDao extends BaseJdbcDao implements IImportSiteDao {
    public Optional<ImportSite> findById(String siteCode) throws SQLException {
        String sql = "SELECT * FROM ImportSite WHERE site_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<ImportSite> findAll() throws SQLException {
        String sql = "SELECT * FROM ImportSite ORDER BY site_code";
        List<ImportSite> results = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(map(resultSet));
            }
        }
        return results;
    }

    public int insert(ImportSite importSite) throws SQLException {
        String sql = "INSERT INTO ImportSite (site_code, site_name, country, contact_info) VALUES (?, ?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, importSite.getSiteCode());
            statement.setString(2, importSite.getSiteName());
            statement.setString(3, importSite.getCountry());
            statement.setString(4, importSite.getContactInfo());
            return statement.executeUpdate();
        }
    }

    public boolean update(ImportSite importSite) throws SQLException {
        String sql = "UPDATE ImportSite SET site_name = ?, country = ?, contact_info = ? WHERE site_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, importSite.getSiteName());
            statement.setString(2, importSite.getCountry());
            statement.setString(3, importSite.getContactInfo());
            statement.setString(4, importSite.getSiteCode());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(String siteCode) throws SQLException {
        String sql = "DELETE FROM ImportSite WHERE site_code = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, siteCode);
            return statement.executeUpdate() > 0;
        }
    }

    private ImportSite map(ResultSet resultSet) throws SQLException {
        return new ImportSite(
                resultSet.getString("site_code"),
                resultSet.getString("site_name"),
                resultSet.getString("country"),
                resultSet.getString("contact_info")
        );
    }
}