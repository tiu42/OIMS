package com.oims.core.dao;

import com.oims.core.model.SiteTransportInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ISiteTransportInfoDao {
    Optional<SiteTransportInfo> findById(int transportId) throws SQLException;
    List<SiteTransportInfo> findAll() throws SQLException;
    List<SiteTransportInfo> findBySiteCode(String siteCode) throws SQLException;
    int insert(SiteTransportInfo siteTransportInfo) throws SQLException;
    boolean update(SiteTransportInfo siteTransportInfo) throws SQLException;
    boolean delete(int transportId) throws SQLException;
}
