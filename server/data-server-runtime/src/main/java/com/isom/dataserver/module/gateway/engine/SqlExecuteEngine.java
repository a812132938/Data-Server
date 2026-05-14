package com.isom.dataserver.module.gateway.engine;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class SqlExecuteEngine {

    public List<Map<String, Object>> execute(Connection conn, BoundSqlResult boundSql, int timeoutSeconds, int maxRows) {
        String sql = boundSql.getRenderedSql();
        if (!sql.toUpperCase().contains("LIMIT")) {
            sql = sql + " LIMIT " + maxRows;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setQueryTimeout(timeoutSeconds);

            if (boundSql.getParameterValues() != null) {
                int index = 1;
                for (Object value : boundSql.getParameterValues()) {
                    ps.setObject(index++, value);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                List<Map<String, Object>> rows = new ArrayList<>();

                while (rs.next() && rows.size() < maxRows) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (SQLTimeoutException e) {
            throw new BizException(ErrorCode.EXEC_TIMEOUT, "SQL 执行超时");
        } catch (SQLException e) {
            throw new BizException(ErrorCode.SQL_EXEC_FAIL, "SQL 执行失败: " + e.getMessage());
        }
    }
}
