package com.oims.core.dao;

import com.oims.core.model.User;
import com.oims.core.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao extends BaseJdbcDao implements IUserDao {
    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT * FROM `User` WHERE user_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<User> findByCredentials(String username, String password, UserRole role) throws SQLException {
        String sql = "SELECT * FROM `User` WHERE username = ? AND password_hash = ? AND role = ? AND is_active = 1";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role.getDbValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM `User` ORDER BY user_id";
        List<User> users = new ArrayList<>();
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(map(resultSet));
            }
        }
        return users;
    }

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO `User` (username, password_hash, full_name, email, role, created_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getRole().getDbValue());
            statement.setDate(6, toDate(user.getCreatedDate()));
            statement.setBoolean(7, user.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                }
            }
        }
        return 0;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE `User` SET username = ?, password_hash = ?, full_name = ?, email = ?, role = ?, created_date = ?, is_active = ? WHERE user_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getRole().getDbValue());
            statement.setDate(6, toDate(user.getCreatedDate()));
            statement.setBoolean(7, user.isActive());
            statement.setInt(8, user.getUserId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM `User` WHERE user_id = ?";
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                UserRole.fromDbValue(resultSet.getString("role")),
                toLocalDate(resultSet.getDate("created_date")),
                resultSet.getBoolean("is_active")
        );
    }
}