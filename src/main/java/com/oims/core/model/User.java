package com.oims.core.model;

import com.oims.core.dao.UserDao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class User {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDate createdDate;
    private boolean active;

    public User() {
    }

    public User(Integer userId, String username, String passwordHash, String fullName, String email, UserRole role, LocalDate createdDate, boolean active) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdDate = createdDate;
        this.active = active;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static Optional<User> authenticate(String username, String password, String roleLabel) {
        UserRole role = resolveRole(roleLabel);
        if (role == null || username == null || password == null) {
            return Optional.empty();
        }

        try {
            return new UserDao().findByCredentials(username.trim(), password, role);
        } catch (SQLException exception) {
            throw new RuntimeException("Không thể kiểm tra thông tin đăng nhập", exception);
        }
    }

    private static UserRole resolveRole(String roleLabel) {
        if (roleLabel == null) {
            return null;
        }

        return switch (roleLabel.trim()) {
            case "Admin" -> UserRole.ADMIN;
            case "NV.BP.Bán hàng" -> UserRole.SALES;
            case "NV.BP.Đặt hàng" -> UserRole.OVERSEAS_ORDER;
            case "NV.BP.Quản lý kho" -> UserRole.WAREHOUSE;
            default -> null;
        };
    }
}