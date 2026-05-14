package com.isom.dataserver.common.spi;

import com.isom.dataserver.module.datasource.vo.SchemaVO;
import com.isom.dataserver.module.datasource.vo.TestResultVO;

import java.sql.Connection;
import java.util.List;

public interface DataSourceDriver {
    String type();
    TestResultVO test(String jdbcUrl, String username, String password, int timeoutSec);
    List<SchemaVO.Schema> getSchemas(Connection conn);
    String buildJdbcUrl(String host, int port, String database, String jdbcParams);
}
