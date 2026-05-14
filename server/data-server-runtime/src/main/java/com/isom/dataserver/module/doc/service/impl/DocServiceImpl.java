package com.isom.dataserver.module.doc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.entity.ApiGroup;
import com.isom.dataserver.module.api.entity.ApiParam;
import com.isom.dataserver.module.api.entity.ApiResponseField;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.api.mapper.ApiGroupMapper;
import com.isom.dataserver.module.api.mapper.ApiParamMapper;
import com.isom.dataserver.module.api.mapper.ApiResponseFieldMapper;
import com.isom.dataserver.module.datasource.service.DatasourcePoolManager;
import com.isom.dataserver.module.doc.service.DocService;
import com.isom.dataserver.module.gateway.engine.BoundSqlResult;
import com.isom.dataserver.module.gateway.engine.SqlExecuteEngine;
import com.isom.dataserver.module.gateway.engine.SqlRenderEngine;
import com.isom.dataserver.module.gateway.masker.DataMasker;
import com.isom.dataserver.module.gateway.masker.MaskResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final ApiDefinitionMapper apiDefinitionMapper;
    private final ApiGroupMapper apiGroupMapper;
    private final ApiParamMapper apiParamMapper;
    private final ApiResponseFieldMapper apiResponseFieldMapper;
    private final SqlRenderEngine sqlRenderEngine;
    private final SqlExecuteEngine sqlExecuteEngine;
    private final DatasourcePoolManager poolManager;
    private final DataMasker dataMasker;

    @Override
    public List<Map<String, Object>> tree() {
        List<ApiGroup> groups = apiGroupMapper.selectList(
                new LambdaQueryWrapper<ApiGroup>().orderByAsc(ApiGroup::getSort));
        List<ApiDefinition> apis = apiDefinitionMapper.selectList(
                new LambdaQueryWrapper<ApiDefinition>().eq(ApiDefinition::getStatus, "PUBLISHED"));

        Map<Long, List<ApiDefinition>> apisByGroup = apis.stream()
                .collect(Collectors.groupingBy(a -> a.getGroupId() != null ? a.getGroupId() : 0L));

        return buildDocTree(groups, apisByGroup, 0L);
    }

    @Override
    public Map<String, Object> detail(Long apiId) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", api.getId());
        result.put("name", api.getName());
        result.put("code", api.getCode());
        result.put("path", api.getPath());
        result.put("method", api.getMethod());
        result.put("description", api.getDescription());

        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId).orderByAsc(ApiParam::getSort));
        result.put("params", params.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", p.getName());
            m.put("location", p.getLocation());
            m.put("dataType", p.getDataType());
            m.put("required", p.getRequired() != null && p.getRequired() == 1);
            m.put("defaultValue", p.getDefaultValue());
            m.put("description", p.getDescription());
            m.put("example", p.getExample());
            return m;
        }).collect(Collectors.toList()));

        List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, apiId).orderByAsc(ApiResponseField::getSort));
        result.put("responseFields", fields.stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fieldName", f.getFieldName());
            m.put("alias", f.getAlias());
            m.put("dataType", f.getDataType());
            m.put("description", f.getDescription());
            return m;
        }).collect(Collectors.toList()));

        // Generate cURL example
        result.put("curlExample", generateCurl(api, params));

        return result;
    }

    @Override
    public Map<String, Object> tryApi(Long apiId, Map<String, Object> params) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        if (!"PUBLISHED".equals(api.getStatus())) {
            throw new BizException(ErrorCode.API_OFFLINE, "API 未发布");
        }

        long start = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        try {
            BoundSqlResult boundSql = sqlRenderEngine.render(api.getSqlTemplate(), params);
            result.put("renderedSql", boundSql.getRenderedSql());

            try (Connection conn = poolManager.getConnection(api.getDatasourceId())) {
                int timeout = api.getTimeoutMs() != null ? api.getTimeoutMs() / 1000 : 30;
                List<Map<String, Object>> rows = sqlExecuteEngine.execute(conn, boundSql, timeout, 100);

                List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                        new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, apiId));
                MaskResult maskResult = dataMasker.mask(rows, fields);

                result.put("data", maskResult.getRows());
                result.put("masked", maskResult.isMasked());
                result.put("status", "SUCCESS");
            }
        } catch (Exception e) {
            result.put("status", "FAIL");
            result.put("errorMessage", e.getMessage());
        }
        result.put("duration", System.currentTimeMillis() - start);
        return result;
    }

    private List<Map<String, Object>> buildDocTree(List<ApiGroup> groups,
                                                    Map<Long, List<ApiDefinition>> apisByGroup, Long parentId) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (ApiGroup g : groups) {
            if (!Objects.equals(g.getParentId(), parentId)) continue;
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", "group_" + g.getId());
            node.put("name", g.getName());
            node.put("type", "group");
            node.put("children", buildDocTree(groups, apisByGroup, g.getId()));

            List<ApiDefinition> groupApis = apisByGroup.getOrDefault(g.getId(), Collections.emptyList());
            List<Map<String, Object>> apiNodes = groupApis.stream().map(a -> {
                Map<String, Object> an = new LinkedHashMap<>();
                an.put("id", "api_" + a.getId());
                an.put("apiId", a.getId());
                an.put("name", a.getName());
                an.put("method", a.getMethod());
                an.put("path", a.getPath());
                an.put("type", "api");
                return an;
            }).collect(Collectors.toList());
            ((List<Map<String, Object>>) node.get("children")).addAll(apiNodes);

            nodes.add(node);
        }

        // APIs without group
        if (parentId == 0L) {
            List<ApiDefinition> ungrouped = apisByGroup.getOrDefault(0L, Collections.emptyList());
            for (ApiDefinition a : ungrouped) {
                Map<String, Object> an = new LinkedHashMap<>();
                an.put("id", "api_" + a.getId());
                an.put("apiId", a.getId());
                an.put("name", a.getName());
                an.put("method", a.getMethod());
                an.put("path", a.getPath());
                an.put("type", "api");
                nodes.add(an);
            }
        }
        return nodes;
    }

    private String generateCurl(ApiDefinition api, List<ApiParam> params) {
        StringBuilder curl = new StringBuilder("curl -X ");
        curl.append(api.getMethod()).append(" ");
        curl.append("'http://localhost:8080/gateway").append(api.getPath());

        List<ApiParam> queryParams = params.stream()
                .filter(p -> "QUERY".equals(p.getLocation())).collect(Collectors.toList());
        if (!queryParams.isEmpty()) {
            curl.append("?");
            curl.append(queryParams.stream()
                    .map(p -> p.getName() + "=" + (p.getExample() != null ? p.getExample() : "{" + p.getName() + "}"))
                    .collect(Collectors.joining("&")));
        }
        curl.append("' \\\n");
        curl.append("  -H 'Authorization: Bearer {token}'");

        return curl.toString();
    }
}
