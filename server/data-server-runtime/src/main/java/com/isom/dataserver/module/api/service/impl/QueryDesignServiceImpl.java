package com.isom.dataserver.module.api.service.impl;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import com.isom.dataserver.module.api.dto.QueryDesignRenderReq;
import com.isom.dataserver.module.api.service.QueryDesignService;
import com.isom.dataserver.module.api.service.SqlSecurityValidator;
import com.isom.dataserver.module.api.vo.QueryDesignRenderResp;
import com.isom.dataserver.module.datasource.service.DatasourceService;
import com.isom.dataserver.module.datasource.vo.SchemaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class QueryDesignServiceImpl implements QueryDesignService {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final int MAX_JOIN_COUNT = 8;

    private final DatasourceService datasourceService;
    private final SqlSecurityValidator sqlSecurityValidator;

    @Override
    public QueryDesignRenderResp render(QueryDesignRenderReq req) {
        validateBasic(req);

        SchemaIndex schemaIndex = loadSchemaIndex(req.getDatasourceId());
        Map<String, String> aliasToTable = buildAliasToTable(req);
        validateAliasTables(aliasToTable, schemaIndex);

        List<String> warnings = new ArrayList<>();
        List<ApiUpsertReq.ApiResponseFieldItem> responseFields = new ArrayList<>();
        List<ApiUpsertReq.ApiParamItem> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT\n");
        appendSelectFields(sql, req, aliasToTable, schemaIndex, responseFields);
        sql.append("\nFROM ").append(req.getMainTable()).append(" ").append(normalizeMainAlias(req));
        appendJoins(sql, req, aliasToTable, schemaIndex, warnings);
        appendFilters(sql, req, aliasToTable, schemaIndex, params);
        appendSorts(sql, req, aliasToTable, schemaIndex);
        if (Boolean.TRUE.equals(req.getPagination())) {
            sql.append("\nLIMIT #{size} OFFSET #{offset}");
            params.add(paramItem("size", "INT", true, "分页大小", null, null, params.size()));
            params.add(paramItem("offset", "INT", true, "分页偏移量", null, null, params.size()));
        }

        String sqlTemplate = sql.toString();
        sqlSecurityValidator.validate(sqlTemplate);

        QueryDesignRenderResp resp = new QueryDesignRenderResp();
        resp.setSqlTemplate(sqlTemplate);
        resp.setParams(params);
        resp.setResponseFields(responseFields);
        resp.setWarnings(warnings);
        return resp;
    }

    private void validateBasic(QueryDesignRenderReq req) {
        if (req == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "查询设计不能为空");
        }
        if (req.getDatasourceId() == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不能为空");
        }
        if (isBlank(req.getMainTable())) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "主表不能为空");
        }
        if (!isSafeIdentifier(req.getMainTable())) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "主表名非法");
        }
        if (!isBlank(req.getMainAlias()) && !isSafeIdentifier(req.getMainAlias())) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "主表别名非法");
        }
        if (req.getJoins() != null && req.getJoins().size() > MAX_JOIN_COUNT) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "关联表数量不能超过 " + MAX_JOIN_COUNT);
        }
        if (req.getSelectFields() == null || req.getSelectFields().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "至少选择一个返回字段");
        }
    }

    private SchemaIndex loadSchemaIndex(Long datasourceId) {
        SchemaVO schemaVO = datasourceService.schemas(datasourceId);
        SchemaIndex index = new SchemaIndex();
        if (schemaVO.getSchemas() == null) {
            return index;
        }
        for (SchemaVO.Schema schema : schemaVO.getSchemas()) {
            if (schema.getTables() == null) {
                continue;
            }
            for (SchemaVO.Table table : schema.getTables()) {
                index.tables.put(table.getName(), table);
                Map<String, SchemaVO.Column> columns = new LinkedHashMap<>();
                if (table.getColumns() != null) {
                    for (SchemaVO.Column column : table.getColumns()) {
                        columns.put(column.getName(), column);
                    }
                }
                index.columns.put(table.getName(), columns);
            }
        }
        return index;
    }

    private Map<String, String> buildAliasToTable(QueryDesignRenderReq req) {
        Map<String, String> aliasToTable = new LinkedHashMap<>();
        aliasToTable.put(normalizeMainAlias(req), req.getMainTable());
        if (req.getJoins() == null) {
            return aliasToTable;
        }
        int index = 2;
        for (QueryDesignRenderReq.JoinItem join : req.getJoins()) {
            if (isBlank(join.getTable()) || !isSafeIdentifier(join.getTable())) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "关联表名非法");
            }
            String alias = isBlank(join.getAlias()) ? "t" + index : join.getAlias();
            if (!isSafeIdentifier(alias)) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "关联表别名非法");
            }
            if (aliasToTable.containsKey(alias)) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "表别名重复: " + alias);
            }
            aliasToTable.put(alias, join.getTable());
            index++;
        }
        return aliasToTable;
    }

    private void validateAliasTables(Map<String, String> aliasToTable, SchemaIndex schemaIndex) {
        for (String table : aliasToTable.values()) {
            if (!schemaIndex.tables.containsKey(table)) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "表不存在: " + table);
            }
        }
    }

    private void appendSelectFields(StringBuilder sql, QueryDesignRenderReq req,
                                    Map<String, String> aliasToTable, SchemaIndex schemaIndex,
                                    List<ApiUpsertReq.ApiResponseFieldItem> responseFields) {
        Set<String> outputNames = new HashSet<>();
        String mainAlias = normalizeMainAlias(req);
        for (int i = 0; i < req.getSelectFields().size(); i++) {
            QueryDesignRenderReq.SelectField field = req.getSelectFields().get(i);
            String alias = isBlank(field.getTableAlias()) ? mainAlias : field.getTableAlias();
            SchemaVO.Column column = requireColumn(alias, field.getField(), aliasToTable, schemaIndex);
            String outputName = isBlank(field.getAlias()) ? alias + "_" + field.getField() : field.getAlias();
            if (!isSafeIdentifier(outputName)) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "输出字段别名非法: " + outputName);
            }
            if (!outputNames.add(outputName)) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "输出字段别名重复: " + outputName);
            }
            if (i > 0) {
                sql.append(",\n");
            }
            sql.append("  ").append(alias).append(".").append(field.getField()).append(" AS ").append(outputName);

            ApiUpsertReq.ApiResponseFieldItem item = new ApiUpsertReq.ApiResponseFieldItem();
            item.setFieldName(outputName);
            item.setAlias(outputName);
            item.setDataType(column.getType());
            item.setDescription(field.getDescription());
            item.setSensitive(Boolean.TRUE.equals(field.getSensitive()));
            item.setSensitiveRule(field.getSensitiveRule());
            item.setSort(i);
            responseFields.add(item);
        }
    }

    private void appendJoins(StringBuilder sql, QueryDesignRenderReq req,
                             Map<String, String> aliasToTable, SchemaIndex schemaIndex,
                             List<String> warnings) {
        if (req.getJoins() == null) {
            return;
        }
        int index = 2;
        for (QueryDesignRenderReq.JoinItem join : req.getJoins()) {
            String joinType = normalizeJoinType(join.getType());
            String alias = isBlank(join.getAlias()) ? "t" + index : join.getAlias();
            if (join.getConditions() == null || join.getConditions().isEmpty()) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "关联表缺少JOIN条件: " + join.getTable());
            }
            sql.append("\n").append(joinType).append(" JOIN ").append(join.getTable()).append(" ").append(alias).append(" ON ");
            for (int i = 0; i < join.getConditions().size(); i++) {
                QueryDesignRenderReq.JoinCondition condition = join.getConditions().get(i);
                FieldRef left = parseFieldRef(condition.getLeftField());
                FieldRef right = parseFieldRef(condition.getRightField());
                requireColumn(left.alias, left.field, aliasToTable, schemaIndex);
                requireColumn(right.alias, right.field, aliasToTable, schemaIndex);
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append(left.alias).append(".").append(left.field)
                        .append(" = ")
                        .append(right.alias).append(".").append(right.field);
                addIndexWarning(left, aliasToTable, schemaIndex, warnings);
                addIndexWarning(right, aliasToTable, schemaIndex, warnings);
            }
            index++;
        }
    }

    private void appendFilters(StringBuilder sql, QueryDesignRenderReq req,
                               Map<String, String> aliasToTable, SchemaIndex schemaIndex,
                               List<ApiUpsertReq.ApiParamItem> params) {
        if (req.getFilters() == null || req.getFilters().isEmpty()) {
            return;
        }
        sql.append("\n<where>");
        String mainAlias = normalizeMainAlias(req);
        for (QueryDesignRenderReq.FilterItem filter : req.getFilters()) {
            String alias = isBlank(filter.getTableAlias()) ? mainAlias : filter.getTableAlias();
            SchemaVO.Column column = requireColumn(alias, filter.getField(), aliasToTable, schemaIndex);
            String paramName = isBlank(filter.getParamName()) ? filter.getField() : filter.getParamName();
            if (!isSafeIdentifier(paramName)) {
                throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "参数名非法: " + paramName);
            }
            String operator = normalizeFilterOperator(filter.getOperator());
            if (Boolean.TRUE.equals(filter.getRequired())) {
                sql.append("\n  AND ").append(filterSql(alias, filter.getField(), operator, paramName));
            } else {
                sql.append("\n  <if test=\"").append(paramName).append(" != null and ").append(paramName).append(" != ''\">");
                sql.append("\n    AND ").append(filterSql(alias, filter.getField(), operator, paramName));
                sql.append("\n  </if>");
            }
            params.add(paramItem(paramName, toParamType(column.getType()), Boolean.TRUE.equals(filter.getRequired()),
                    filter.getDescription(), filter.getDefaultValue(), filter.getExample(), params.size()));
        }
        sql.append("\n</where>");
    }

    private void appendSorts(StringBuilder sql, QueryDesignRenderReq req,
                             Map<String, String> aliasToTable, SchemaIndex schemaIndex) {
        if (req.getSorts() == null || req.getSorts().isEmpty()) {
            return;
        }
        sql.append("\nORDER BY ");
        String mainAlias = normalizeMainAlias(req);
        for (int i = 0; i < req.getSorts().size(); i++) {
            QueryDesignRenderReq.SortItem sort = req.getSorts().get(i);
            String alias = isBlank(sort.getTableAlias()) ? mainAlias : sort.getTableAlias();
            requireColumn(alias, sort.getField(), aliasToTable, schemaIndex);
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(alias).append(".").append(sort.getField()).append(" ").append(normalizeDirection(sort.getDirection()));
        }
    }

    private String filterSql(String alias, String field, String operator, String paramName) {
        if ("IN".equals(operator)) {
            return alias + "." + field + " IN <foreach collection=\"" + paramName + "\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>";
        }
        return alias + "." + field + " " + operator + " #{" + paramName + "}";
    }

    private ApiUpsertReq.ApiParamItem paramItem(String name, String dataType, boolean required,
                                                String description, String defaultValue, String example, int sort) {
        ApiUpsertReq.ApiParamItem item = new ApiUpsertReq.ApiParamItem();
        item.setName(name);
        item.setLocation("QUERY");
        item.setDataType(dataType);
        item.setRequired(required);
        item.setDefaultValue(defaultValue);
        item.setExample(example);
        item.setDescription(description);
        item.setSort(sort);
        return item;
    }

    private SchemaVO.Column requireColumn(String alias, String field, Map<String, String> aliasToTable, SchemaIndex schemaIndex) {
        if (isBlank(alias) || !aliasToTable.containsKey(alias)) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "表别名不存在: " + alias);
        }
        if (isBlank(field) || !isSafeIdentifier(field)) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "字段名非法: " + field);
        }
        String table = aliasToTable.get(alias);
        Map<String, SchemaVO.Column> columns = schemaIndex.columns.get(table);
        if (columns == null || !columns.containsKey(field)) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "字段不存在: " + alias + "." + field);
        }
        return columns.get(field);
    }

    private FieldRef parseFieldRef(String value) {
        if (isBlank(value) || value.indexOf('.') < 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "JOIN字段格式必须是 别名.字段: " + value);
        }
        String[] parts = value.split("\\.", 2);
        if (!isSafeIdentifier(parts[0]) || !isSafeIdentifier(parts[1])) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "JOIN字段非法: " + value);
        }
        return new FieldRef(parts[0], parts[1]);
    }

    private void addIndexWarning(FieldRef ref, Map<String, String> aliasToTable, SchemaIndex schemaIndex, List<String> warnings) {
        String tableName = aliasToTable.get(ref.alias);
        SchemaVO.Table table = schemaIndex.tables.get(tableName);
        if (table == null || table.getIndexes() == null) {
            return;
        }
        for (SchemaVO.Index index : table.getIndexes()) {
            if (ref.field.equals(index.getColumnName())) {
                return;
            }
        }
        warnings.add("JOIN字段缺少索引: " + tableName + "." + ref.field);
    }

    private String normalizeMainAlias(QueryDesignRenderReq req) {
        return isBlank(req.getMainAlias()) ? "t1" : req.getMainAlias();
    }

    private String normalizeJoinType(String type) {
        if (isBlank(type)) {
            return "LEFT";
        }
        String normalized = type.trim().toUpperCase();
        if ("INNER".equals(normalized) || "LEFT".equals(normalized)) {
            return normalized;
        }
        throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "JOIN类型仅支持 INNER/LEFT");
    }

    private String normalizeFilterOperator(String operator) {
        if (isBlank(operator)) {
            return "=";
        }
        String normalized = operator.trim().toUpperCase();
        if ("EQ".equals(normalized)) return "=";
        if ("NE".equals(normalized)) return "!=";
        if ("GT".equals(normalized)) return ">";
        if ("GE".equals(normalized)) return ">=";
        if ("LT".equals(normalized)) return "<";
        if ("LE".equals(normalized)) return "<=";
        if ("LIKE".equals(normalized)) return "LIKE";
        if ("IN".equals(normalized)) return "IN";
        throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "不支持的过滤操作符: " + operator);
    }

    private String normalizeDirection(String direction) {
        if (isBlank(direction)) {
            return "ASC";
        }
        String normalized = direction.trim().toUpperCase();
        if ("ASC".equals(normalized) || "DESC".equals(normalized)) {
            return normalized;
        }
        throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "排序方向仅支持 ASC/DESC");
    }

    private String toParamType(String dbType) {
        if (dbType == null) {
            return "STRING";
        }
        String type = dbType.toLowerCase();
        if (type.contains("bigint")) return "LONG";
        if (type.contains("int")) return "INT";
        if (type.contains("decimal") || type.contains("numeric") || type.contains("double") || type.contains("float")) return "DECIMAL";
        if (type.contains("date") || type.contains("time")) return "DATETIME";
        return "STRING";
    }

    private boolean isSafeIdentifier(String value) {
        return value != null && IDENTIFIER.matcher(value).matches();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class SchemaIndex {
        private final Map<String, SchemaVO.Table> tables = new LinkedHashMap<>();
        private final Map<String, Map<String, SchemaVO.Column>> columns = new LinkedHashMap<>();
    }

    private static class FieldRef {
        private final String alias;
        private final String field;

        private FieldRef(String alias, String field) {
            this.alias = alias;
            this.field = field;
        }
    }
}
