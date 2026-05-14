package com.isom.dataserver.module.api.service.impl;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import com.isom.dataserver.module.api.dto.SqlPreviewReq;
import com.isom.dataserver.module.api.service.SqlPreviewService;
import com.isom.dataserver.module.api.service.SqlSecurityValidator;
import com.isom.dataserver.module.api.vo.SqlPreviewResp;
import com.isom.dataserver.module.datasource.service.DatasourcePoolManager;
import com.isom.dataserver.module.gateway.engine.BoundSqlResult;
import com.isom.dataserver.module.gateway.engine.SqlRenderEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SqlPreviewServiceImpl implements SqlPreviewService {

    private static final int DEFAULT_MAX_ROWS = 20;
    private static final int HARD_MAX_ROWS = 100;

    private final DatasourcePoolManager poolManager;
    private final SqlRenderEngine sqlRenderEngine;
    private final SqlSecurityValidator sqlSecurityValidator;

    @Override
    public SqlPreviewResp preview(SqlPreviewReq req) {
        if (req == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "预览请求不能为空");
        }
        if (req.getDatasourceId() == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不能为空");
        }
        if (req.getSqlTemplate() == null || req.getSqlTemplate().trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "SQL模板不能为空");
        }

        sqlSecurityValidator.validate(req.getSqlTemplate());
        BoundSqlResult boundSql = sqlRenderEngine.render(req.getSqlTemplate(),
                req.getParams() != null ? req.getParams() : new HashMap<String, Object>());

        List<String> warnings = new ArrayList<>();
        int maxRows = normalizeMaxRows(req.getMaxRows(), warnings);
        int timeoutSeconds = req.getTimeoutMs() != null && req.getTimeoutMs() > 0
                ? Math.max(1, req.getTimeoutMs() / 1000) : 5;
        String effectiveSql = appendLimit(boundSql.getRenderedSql(), maxRows);

        try (Connection conn = poolManager.getConnection(req.getDatasourceId());
             PreparedStatement ps = conn.prepareStatement(effectiveSql)) {
            ps.setQueryTimeout(timeoutSeconds);
            bindParams(ps, boundSql.getParameterValues());

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                SqlPreviewResp resp = new SqlPreviewResp();
                resp.setRenderedSql(effectiveSql);
                resp.setParameterMappings(boundSql.getParameterMappings());
                resp.setResponseFields(toResponseFields(meta));
                resp.setRows(readRows(rs, meta, maxRows));
                resp.setWarnings(warnings);
                return resp;
            }
        } catch (SQLTimeoutException e) {
            throw new BizException(ErrorCode.EXEC_TIMEOUT, "SQL 预览执行超时");
        } catch (SQLException e) {
            throw new BizException(ErrorCode.SQL_EXEC_FAIL, "SQL 预览执行失败: " + e.getMessage());
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> values) throws SQLException {
        if (values == null) {
            return;
        }
        int index = 1;
        for (Object value : values) {
            ps.setObject(index++, value);
        }
    }

    private List<ApiUpsertReq.ApiResponseFieldItem> toResponseFields(ResultSetMetaData meta) throws SQLException {
        List<ApiUpsertReq.ApiResponseFieldItem> fields = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String label = meta.getColumnLabel(i);
            ApiUpsertReq.ApiResponseFieldItem item = new ApiUpsertReq.ApiResponseFieldItem();
            item.setFieldName(label);
            item.setAlias(label);
            item.setDataType(meta.getColumnTypeName(i));
            item.setDescription(meta.getTableName(i) != null && !meta.getTableName(i).isEmpty()
                    ? meta.getTableName(i) + "." + meta.getColumnName(i) : meta.getColumnName(i));
            item.setSensitive(false);
            item.setSort(i - 1);
            fields.add(item);
        }
        return fields;
    }

    private List<Map<String, Object>> readRows(ResultSet rs, ResultSetMetaData meta, int maxRows) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        int colCount = meta.getColumnCount();
        while (rs.next() && rows.size() < maxRows) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= colCount; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    private int normalizeMaxRows(Integer requested, List<String> warnings) {
        if (requested == null || requested <= 0) {
            return DEFAULT_MAX_ROWS;
        }
        if (requested > HARD_MAX_ROWS) {
            warnings.add("预览最大行数已限制为 " + HARD_MAX_ROWS);
            return HARD_MAX_ROWS;
        }
        return requested;
    }

    private String appendLimit(String sql, int maxRows) {
        String trimmed = sql == null ? "" : sql.trim();
        while (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        if (trimmed.toUpperCase().contains("LIMIT")) {
            return trimmed;
        }
        return trimmed + " LIMIT " + maxRows;
    }
}
