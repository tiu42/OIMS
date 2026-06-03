package com.oims.core.dao;

import com.oims.core.model.SiteMerchandise;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ISiteMerchandiseDao {
    Optional<SiteMerchandise> findById(String siteCode, String merchandiseCode) throws SQLException;
    List<SiteMerchandise> findAll() throws SQLException;
    List<SiteMerchandise> findBySiteCode(String siteCode) throws SQLException;
    List<SiteMerchandise> findByMerchandiseCode(String merchandiseCode) throws SQLException;
    int insert(SiteMerchandise siteMerchandise) throws SQLException;
    boolean update(SiteMerchandise siteMerchandise) throws SQLException;
    boolean delete(String siteCode, String merchandiseCode) throws SQLException;
}
