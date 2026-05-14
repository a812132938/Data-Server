package com.isom.dataserver.module.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.spi.SqlTemplateParser;
import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.entity.ApiParam;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.api.mapper.ApiParamMapper;
import com.isom.dataserver.module.api.service.SqlParserService;
import com.isom.dataserver.module.api.service.SqlSecurityValidator;
import com.isom.dataserver.module.api.vo.ParseParamsResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SqlParserServiceImpl implements SqlParserService {

    private final ApiDefinitionMapper apiDefinitionMapper;
    private final ApiParamMapper apiParamMapper;
    private final SqlTemplateParser sqlTemplateParser;
    private final SqlSecurityValidator sqlSecurityValidator;

    @Override
    public ParseParamsResp parse(Long apiId, String sqlTemplate) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");

        if (sqlTemplate == null || sqlTemplate.isEmpty()) {
            sqlTemplate = api.getSqlTemplate();
        }
        sqlSecurityValidator.validate(sqlTemplate);

        List<String> parsedNames = sqlTemplateParser.parseParamNames(sqlTemplate);

        List<ApiParam> existingParams = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId).orderByAsc(ApiParam::getSort));
        Map<String, ApiParam> existingMap = existingParams.stream()
                .collect(Collectors.toMap(ApiParam::getName, p -> p, (a, b) -> a));

        List<ApiUpsertReq.ApiParamItem> resultParams = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        int sortIndex = 0;
        for (String name : parsedNames) {
            ApiParam existing = existingMap.remove(name);
            ApiUpsertReq.ApiParamItem item = new ApiUpsertReq.ApiParamItem();
            if (existing != null) {
                item.setName(existing.getName());
                item.setLocation(existing.getLocation());
                item.setDataType(existing.getDataType());
                item.setRequired(existing.getRequired() == 1);
                item.setDefaultValue(existing.getDefaultValue());
                item.setOperator(existing.getOperator());
                item.setValidateRule(existing.getValidateRule());
                item.setValidateMin(existing.getValidateMin());
                item.setValidateMax(existing.getValidateMax());
                item.setFormatDesc(existing.getFormatDesc());
                item.setDescription(existing.getDescription());
                item.setExample(existing.getExample());
                item.setSort(existing.getSort());
            } else {
                item.setName(name);
                item.setLocation("QUERY");
                item.setDataType("STRING");
                item.setRequired(false);
                item.setSort(sortIndex);
            }
            resultParams.add(item);
            sortIndex++;
        }

        for (String missing : existingMap.keySet()) {
            warnings.add("参数 '" + missing + "' 在 SQL 模板中未找到");
        }

        ParseParamsResp resp = new ParseParamsResp();
        resp.setParams(resultParams);
        resp.setWarnings(warnings);
        return resp;
    }

    @Override
    public List<Map<String, Object>> listParams(Long apiId) {
        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId).orderByAsc(ApiParam::getSort));
        return params.stream().map(this::paramToMap).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> batchSave(Long apiId, List<Map<String, Object>> params) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        if (!"DRAFT".equals(api.getStatus()) && !"TESTING".equals(api.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATUS, "仅 DRAFT/TESTING 状态可保存参数");
        }

        apiParamMapper.delete(new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId));

        int count = 0;
        if (params != null) {
            for (Map<String, Object> p : params) {
                ApiParam entity = new ApiParam();
                entity.setApiId(apiId);
                entity.setName((String) p.get("name"));
                entity.setLocation((String) p.getOrDefault("location", "QUERY"));
                entity.setDataType((String) p.getOrDefault("dataType", "STRING"));
                entity.setRequired(Boolean.TRUE.equals(p.get("required")) ? 1 : 0);
                entity.setDefaultValue((String) p.get("defaultValue"));
                entity.setOperator((String) p.get("operator"));
                entity.setValidateRule((String) p.get("validateRule"));
                entity.setValidateMin((String) p.get("validateMin"));
                entity.setValidateMax((String) p.get("validateMax"));
                entity.setFormatDesc((String) p.get("formatDesc"));
                entity.setDescription((String) p.get("description"));
                entity.setExample((String) p.get("example"));
                entity.setSort(p.get("sort") != null ? ((Number) p.get("sort")).intValue() : count);
                apiParamMapper.insert(entity);
                count++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    private Map<String, Object> paramToMap(ApiParam p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", p.getName());
        m.put("location", p.getLocation());
        m.put("dataType", p.getDataType());
        m.put("required", p.getRequired() == 1);
        m.put("defaultValue", p.getDefaultValue());
        m.put("operator", p.getOperator());
        m.put("validateRule", p.getValidateRule());
        m.put("validateMin", p.getValidateMin());
        m.put("validateMax", p.getValidateMax());
        m.put("formatDesc", p.getFormatDesc());
        m.put("description", p.getDescription());
        m.put("example", p.getExample());
        m.put("sort", p.getSort());
        return m;
    }
}
