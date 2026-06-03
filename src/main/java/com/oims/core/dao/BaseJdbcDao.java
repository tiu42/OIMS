package com.oims.core.dao;

import com.oims.core.database.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

abstract class BaseJdbcDao {
    protected Connection connection() throws SQLException {
        return DBConnection.getConnection();
    }

    protected Date toDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    protected LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}