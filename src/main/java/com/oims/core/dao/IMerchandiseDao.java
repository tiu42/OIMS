package com.oims.core.dao;

import com.oims.core.model.Merchandise;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IMerchandiseDao {
    Optional<Merchandise> findById(String merchandiseCode) throws SQLException;
    List<Merchandise> findAll() throws SQLException;
    int insert(Merchandise merchandise) throws SQLException;
    boolean update(Merchandise merchandise) throws SQLException;
    boolean delete(String merchandiseCode) throws SQLException;
}
