package com.isom.dataserver.module.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.api.dto.ApiQuery;
import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import com.isom.dataserver.module.api.dto.CloneReq;
import com.isom.dataserver.module.api.entity.*;
import com.isom.dataserver.module.api.mapper.*;
import com.isom.dataserver.module.api.service.ApiDefinitionService;
import com.isom.dataserver.module.api.service.CodeGenerator;
import com.isom.dataserver.module.api.service.SqlSecurityValidator;
import com.isom.dataserver.module.api.vo.ApiSummaryVO;
import com.isom.dataserver.module.api.vo.ApiVO;
import com.isom.dataserver.module.datasource.entity.DataSource;
import com.isom.dataserver.module.datasource.mapper.DataSourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiDefinitionServiceImpl implements ApiDefinitionService {

    private final ApiDefinitionMapper apiDefinitionMapper;
    private final ApiParamMapper apiParamMapper;
    private final ApiResponseFieldMapper apiResponseFieldMapper;
    private final ApiVersionMapper apiVersionMapper;
    private final ApiGroupMapper apiGroupMapper;
    private final DataSourceMapper dataSourceMapper;
    private final CodeGenerator codeGenerator;
    private final SqlSecurityValidator sqlSecurityValidator;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> create(ApiUpsertReq req) {
        checkPathMethodUnique(req.getPath(), req.getMethod(), null);
        if (req.getSqlTemplate() != null && !req.getSqlTemplate().isEmpty()) {
            sqlSecurityValidator.validate(req.getSqlTemplate());
        }

        ApiDefinition entity = new ApiDefinition();
        entity.setGroupId(req.getGroupId());
        entity.setName(req.getName());
        entity.setCode(codeGenerator.nextApiCode());
        entity.setPath(req.getPath());
        entity.setMethod(req.getMethod() != null ? req.getMethod().toUpperCase() : "GET");
        entity.setCreateMode(req.getCreateMode() != null ? req.getCreateMode() : "SQL");
        entity.setDatasourceId(req.getDatasourceId());
        entity.setSqlTemplate(req.getSqlTemplate());
        entity.setSqlType(req.getSqlType() != null ? req.getSqlType() : "SELECT");
        entity.setTimeoutMs(req.getTimeoutMs() != null ? req.getTimeoutMs() : 30000);
        entity.setCacheEnable(Boolean.TRUE.equals(req.getCacheEnable()) ? 1 : 0);
        entity.setCacheTtl(req.getCacheTtl());
        entity.setDefaultQpsLimit(req.getDefaultQpsLimit() != null ? req.getDefaultQpsLimit() : 100);
        entity.setStatus("DRAFT");
        entity.setDescription(req.getDescription());
        entity.setOwner(1L);
        apiDefinitionMapper.insert(entity);

        saveParams(entity.getId(), req.getParams());
        saveResponseFields(entity.getId(), req.getResponseFields());

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        result.put("code", entity.getCode());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> update(Long id, ApiUpsertReq req) {
        ApiDefinition entity = apiDefinitionMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        if ("OFFLINE".equals(entity.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATUS, "已下线的 API 不可编辑");
        }

        checkPathMethodUnique(req.getPath(), req.getMethod(), id);
        if (req.getSqlTemplate() != null && !req.getSqlTemplate().isEmpty()) {
            sqlSecurityValidator.validate(req.getSqlTemplate());
        }

        entity.setGroupId(req.getGroupId());
        entity.setName(req.getName());
        entity.setPath(req.getPath());
        entity.setMethod(req.getMethod() != null ? req.getMethod().toUpperCase() : entity.getMethod());
        entity.setDatasourceId(req.getDatasourceId());
        entity.setSqlTemplate(req.getSqlTemplate());
        entity.setSqlType(req.getSqlType());
        entity.setTimeoutMs(req.getTimeoutMs());
        entity.setCacheEnable(Boolean.TRUE.equals(req.getCacheEnable()) ? 1 : 0);
        entity.setCacheTtl(req.getCacheTtl());
        entity.setDefaultQpsLimit(req.getDefaultQpsLimit());
        entity.setDescription(req.getDescription());
        apiDefinitionMapper.updateById(entity);

        if (req.getParams() != null) {
            apiParamMapper.delete(new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, id));
            saveParams(id, req.getParams());
        }
        if (req.getResponseFields() != null) {
            apiResponseFieldMapper.delete(new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, id));
            saveResponseFields(id, req.getResponseFields());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ApiDefinition entity = apiDefinitionMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        if ("PUBLISHED".equals(entity.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATUS, "已发布的 API 不能直接删除，请先下线");
        }
        apiParamMapper.delete(new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, id));
        apiResponseFieldMapper.delete(new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, id));
        apiDefinitionMapper.deleteById(id);
    }

    @Override
    public PageResult<ApiVO> list(ApiQuery query) {
        LambdaQueryWrapper<ApiDefinition> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(ApiDefinition::getName, query.getKeyword())
                    .or().like(ApiDefinition::getCode, query.getKeyword())
                    .or().like(ApiDefinition::getPath, query.getKeyword()));
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(ApiDefinition::getStatus, query.getStatus());
        }
        if (query.getGroupId() != null) {
            wrapper.eq(ApiDefinition::getGroupId, query.getGroupId());
        }
        wrapper.orderByDesc(ApiDefinition::getCreatedAt);

        Page<ApiDefinition> page = apiDefinitionMapper.selectPage(query.toPage(), wrapper);
        List<ApiVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public ApiVO detail(Long id) {
        ApiDefinition entity = apiDefinitionMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        ApiVO vo = toVO(entity);

        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, id).orderByAsc(ApiParam::getSort));
        vo.setParams(params.stream().map(this::paramToItem).collect(Collectors.toList()));

        List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, id).orderByAsc(ApiResponseField::getSort));
        vo.setResponseFields(fields.stream().map(this::fieldToItem).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    public Map<String, Object> clone(Long id, CloneReq req) {
        ApiDefinition source = apiDefinitionMapper.selectById(id);
        if (source == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        checkPathMethodUnique(req.getPath(), source.getMethod(), null);

        ApiDefinition cloned = new ApiDefinition();
        cloned.setGroupId(source.getGroupId());
        cloned.setName(req.getName());
        cloned.setCode(codeGenerator.nextApiCode());
        cloned.setPath(req.getPath());
        cloned.setMethod(source.getMethod());
        cloned.setCreateMode(source.getCreateMode());
        cloned.setDatasourceId(source.getDatasourceId());
        cloned.setSqlTemplate(source.getSqlTemplate());
        cloned.setSqlType(source.getSqlType());
        cloned.setTimeoutMs(source.getTimeoutMs());
        cloned.setCacheEnable(source.getCacheEnable());
        cloned.setCacheTtl(source.getCacheTtl());
        cloned.setDefaultQpsLimit(source.getDefaultQpsLimit());
        cloned.setStatus("DRAFT");
        cloned.setDescription(source.getDescription());
        cloned.setOwner(1L);
        apiDefinitionMapper.insert(cloned);

        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, id));
        for (ApiParam p : params) {
            p.setId(null);
            p.setApiId(cloned.getId());
            p.setCreatedAt(null);
            p.setUpdatedAt(null);
            apiParamMapper.insert(p);
        }

        List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, id));
        for (ApiResponseField f : fields) {
            f.setId(null);
            f.setApiId(cloned.getId());
            f.setCreatedAt(null);
            f.setUpdatedAt(null);
            apiResponseFieldMapper.insert(f);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", cloned.getId());
        result.put("code", cloned.getCode());
        return result;
    }

    @Override
    public ApiSummaryVO summary() {
        List<Map<String, Object>> statusCounts = apiDefinitionMapper.countByStatus();
        ApiSummaryVO vo = new ApiSummaryVO();
        int total = 0;
        for (Map<String, Object> row : statusCounts) {
            String status = (String) row.get("status");
            int cnt = ((Number) row.get("cnt")).intValue();
            total += cnt;
            if ("DRAFT".equals(status)) vo.setDraft(cnt);
            else if ("TESTING".equals(status)) vo.setTesting(cnt);
            else if ("PUBLISHED".equals(status)) vo.setPublished(cnt);
            else if ("OFFLINE".equals(status)) vo.setOffline(cnt);
        }
        vo.setTotal(total);
        return vo;
    }

    private void checkPathMethodUnique(String path, String method, Long excludeId) {
        LambdaQueryWrapper<ApiDefinition> wrapper = new LambdaQueryWrapper<ApiDefinition>()
                .eq(ApiDefinition::getPath, path)
                .eq(ApiDefinition::getMethod, method != null ? method.toUpperCase() : "GET");
        if (excludeId != null) {
            wrapper.ne(ApiDefinition::getId, excludeId);
        }
        if (apiDefinitionMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "路径+方法组合已存在");
        }
    }

    private void saveParams(Long apiId, List<ApiUpsertReq.ApiParamItem> params) {
        if (params == null) return;
        int sort = 0;
        for (ApiUpsertReq.ApiParamItem item : params) {
            ApiParam entity = new ApiParam();
            entity.setApiId(apiId);
            entity.setName(item.getName());
            entity.setLocation(item.getLocation() != null ? item.getLocation() : "QUERY");
            entity.setDataType(item.getDataType() != null ? item.getDataType() : "STRING");
            entity.setRequired(Boolean.TRUE.equals(item.getRequired()) ? 1 : 0);
            entity.setDefaultValue(item.getDefaultValue());
            entity.setOperator(item.getOperator());
            entity.setValidateRule(item.getValidateRule());
            entity.setValidateMin(item.getValidateMin());
            entity.setValidateMax(item.getValidateMax());
            entity.setFormatDesc(item.getFormatDesc());
            entity.setDescription(item.getDescription());
            entity.setExample(item.getExample());
            entity.setSort(item.getSort() != null ? item.getSort() : sort);
            apiParamMapper.insert(entity);
            sort++;
        }
    }

    private void saveResponseFields(Long apiId, List<ApiUpsertReq.ApiResponseFieldItem> fields) {
        if (fields == null) return;
        int sort = 0;
        for (ApiUpsertReq.ApiResponseFieldItem item : fields) {
            ApiResponseField entity = new ApiResponseField();
            entity.setApiId(apiId);
            entity.setFieldName(item.getFieldName());
            entity.setAlias(item.getAlias());
            entity.setDataType(item.getDataType());
            entity.setDescription(item.getDescription());
            entity.setSensitive(Boolean.TRUE.equals(item.getSensitive()) ? 1 : 0);
            entity.setSensitiveRule(item.getSensitiveRule());
            entity.setSort(item.getSort() != null ? item.getSort() : sort);
            apiResponseFieldMapper.insert(entity);
            sort++;
        }
    }

    private ApiVO toVO(ApiDefinition entity) {
        ApiVO vo = new ApiVO();
        vo.setId(entity.getId());
        vo.setGroupId(entity.getGroupId());
        vo.setName(entity.getName());
        vo.setCode(entity.getCode());
        vo.setPath(entity.getPath());
        vo.setMethod(entity.getMethod());
        vo.setCreateMode(entity.getCreateMode());
        vo.setDatasourceId(entity.getDatasourceId());
        vo.setSqlTemplate(entity.getSqlTemplate());
        vo.setSqlType(entity.getSqlType());
        vo.setTimeoutMs(entity.getTimeoutMs());
        vo.setCacheEnable(entity.getCacheEnable() != null && entity.getCacheEnable() == 1);
        vo.setCacheTtl(entity.getCacheTtl());
        vo.setDefaultQpsLimit(entity.getDefaultQpsLimit());
        vo.setStatus(entity.getStatus());
        vo.setCurrentVersionId(entity.getCurrentVersionId());
        if (entity.getCurrentVersionId() != null) {
            ApiVersion ver = apiVersionMapper.selectById(entity.getCurrentVersionId());
            if (ver != null) vo.setVersion(ver.getVersionNo());
        }
        vo.setDescription(entity.getDescription());
        vo.setOwner(entity.getOwner());
        vo.setOwnerName("管理员");

        if (entity.getGroupId() != null) {
            ApiGroup group = apiGroupMapper.selectById(entity.getGroupId());
            if (group != null) vo.setGroupName(group.getName());
        }
        if (entity.getDatasourceId() != null) {
            DataSource ds = dataSourceMapper.selectById(entity.getDatasourceId());
            if (ds != null) vo.setDatasourceName(ds.getName());
        }
        if (entity.getCreatedAt() != null) vo.setCreatedAt(entity.getCreatedAt().format(FMT));
        if (entity.getUpdatedAt() != null) vo.setUpdatedAt(entity.getUpdatedAt().format(FMT));
        return vo;
    }

    private ApiUpsertReq.ApiParamItem paramToItem(ApiParam p) {
        ApiUpsertReq.ApiParamItem item = new ApiUpsertReq.ApiParamItem();
        item.setName(p.getName());
        item.setLocation(p.getLocation());
        item.setDataType(p.getDataType());
        item.setRequired(p.getRequired() == 1);
        item.setDefaultValue(p.getDefaultValue());
        item.setOperator(p.getOperator());
        item.setValidateRule(p.getValidateRule());
        item.setValidateMin(p.getValidateMin());
        item.setValidateMax(p.getValidateMax());
        item.setFormatDesc(p.getFormatDesc());
        item.setDescription(p.getDescription());
        item.setExample(p.getExample());
        item.setSort(p.getSort());
        return item;
    }

    private ApiUpsertReq.ApiResponseFieldItem fieldToItem(ApiResponseField f) {
        ApiUpsertReq.ApiResponseFieldItem item = new ApiUpsertReq.ApiResponseFieldItem();
        item.setFieldName(f.getFieldName());
        item.setAlias(f.getAlias());
        item.setDataType(f.getDataType());
        item.setDescription(f.getDescription());
        item.setSensitive(f.getSensitive() != null && f.getSensitive() == 1);
        item.setSensitiveRule(f.getSensitiveRule());
        item.setSort(f.getSort());
        return item;
    }
}
