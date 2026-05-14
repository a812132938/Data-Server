package com.isom.dataserver.module.gateway.engine;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SqlRenderEngine {

    private final Configuration configuration = new Configuration();
    private final XMLLanguageDriver languageDriver = new XMLLanguageDriver();

    private static final Map<String, String> OP_MAP = new LinkedHashMap<>();
    static {
        OP_MAP.put("NE", "!=");
        OP_MAP.put("GE", ">=");
        OP_MAP.put("LE", "<=");
        OP_MAP.put("GT", ">");
        OP_MAP.put("LT", "<");
        OP_MAP.put("EQ", "=");
        OP_MAP.put("IN", "IN");
        OP_MAP.put("LIKE", "LIKE");
    }

    public BoundSqlResult render(String sqlTemplate, Map<String, Object> params) {
        try {
            String script = "<script>" + sqlTemplate + "</script>";
            SqlSource sqlSource = languageDriver.createSqlSource(configuration, script, Map.class);
            BoundSql boundSql = sqlSource.getBoundSql(params != null ? params : new HashMap<>());

            BoundSqlResult result = new BoundSqlResult();
            result.setRenderedSql(translateOperators(boundSql.getSql()));
            result.setParameterObject(params);

            List<Map<String, Object>> mappings = new ArrayList<>();
            if (boundSql.getParameterMappings() != null) {
                MetaObject metaObject = params != null ? configuration.newMetaObject(params) : null;
                for (ParameterMapping pm : boundSql.getParameterMappings()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("property", pm.getProperty());
                    m.put("javaType", pm.getJavaType() != null ? pm.getJavaType().getSimpleName() : "Object");
                    mappings.add(m);
                    result.getParameterValues().add(resolveParameterValue(boundSql, pm, metaObject));
                }
            }
            result.setParameterMappings(mappings);
            return result;
        } catch (Exception e) {
            throw new BizException(ErrorCode.SQL_SYNTAX_ERROR, "SQL 渲染失败: " + e.getMessage());
        }
    }

    private String translateOperators(String sql) {
        for (Map.Entry<String, String> entry : OP_MAP.entrySet()) {
            // Match word-boundary operator (e.g. "column_name EQ ?")
            sql = sql.replaceAll("(?i)\\b" + entry.getKey() + "\\b(?=\\s+[?(%])", entry.getValue());
        }
        return sql;
    }

    private Object resolveParameterValue(BoundSql boundSql, ParameterMapping parameterMapping, MetaObject metaObject) {
        String property = parameterMapping.getProperty();
        if (boundSql.hasAdditionalParameter(property)) {
            return boundSql.getAdditionalParameter(property);
        }
        if (metaObject != null && metaObject.hasGetter(property)) {
            return metaObject.getValue(property);
        }
        return null;
    }
}
