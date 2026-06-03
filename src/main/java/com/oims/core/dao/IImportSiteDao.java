package com.oims.core.dao;

import com.oims.core.model.ImportSite;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IImportSiteDao {
    Optional<ImportSite> findById(String siteCode) throws SQLException;
    List<ImportSite> findAll() throws SQLException;
    int insert(ImportSite importSite) throws SQLException;
    boolean update(ImportSite importSite) throws SQLException;
    boolean delete(String siteCode) throws SQLException;
}
