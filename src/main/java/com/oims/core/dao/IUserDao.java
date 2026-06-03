package com.oims.core.dao;

import com.oims.core.model.User;
import com.oims.core.model.UserRole;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IUserDao {
    Optional<User> findById(int userId) throws SQLException;
    Optional<User> findByCredentials(String username, String password, UserRole role) throws SQLException;
    List<User> findAll() throws SQLException;
    int insert(User user) throws SQLException;
    boolean update(User user) throws SQLException;
    boolean delete(int userId) throws SQLException;
}
