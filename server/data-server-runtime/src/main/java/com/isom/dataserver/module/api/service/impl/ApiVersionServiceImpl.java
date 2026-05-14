package com.isom.dataserver.module.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.dto.PublishReq;
import com.isom.dataserver.module.api.dto.RollbackReq;
import com.isom.dataserver.module.api.entity.*;
import com.isom.dataserver.module.api.mapper.*;
import com.isom.dataserver.module.api.service.ApiVersionService;
import com.isom.dataserver.module.api.vo.ApiVersionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiVersionServiceImpl implements ApiVersionService {

    private final ApiDefinitionMapper apiDefinitionMapper;
    private final ApiParamMapper apiParamMapper;
    private final ApiResponseFieldMapper apiResponseFieldMapper;
    private final ApiVersionMapper apiVersionMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> publish(Long apiId, PublishReq req) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        if (!"TESTING".equals(api.getStatus()) && !"PUBLISHED".equals(api.getStatus())) {
            throw new BizException(ErrorCode.GATE_NOT_PASS, "仅 TESTING/PUBLISHED 状态可发布");
        }

        // Build snapshot
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("api", api);
        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId));
        snapshot.put("params", params);
        List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, apiId));
        snapshot.put("responseFields", fields);

        int maxNo = apiVersionMapper.getMaxVersionNo(apiId);
        String versionNo = "v" + (maxNo + 1);

        // 将旧的 PUBLISHED 版本置为 HISTORY
        apiVersionMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ApiVersion>()
                        .eq(ApiVersion::getApiId, apiId)
                        .eq(ApiVersion::getStatus, "PUBLISHED")
                        .set(ApiVersion::getStatus, "HISTORY"));

        ApiVersion version = new ApiVersion();
        version.setApiId(apiId);
        version.setVersionNo(versionNo);
        try {
            version.setSnapshot(objectMapper.writeValueAsString(snapshot));
        } catch (Exception e) {
            throw new BizException(ErrorCode.SNAPSHOT_FAIL, "快照序列化失败");
        }
        version.setChangeLog(req.getChangeLog());
        version.setStatus("PUBLISHED");
        version.setPublishedBy(1L);
        version.setPublishedAt(LocalDateTime.now());
        apiVersionMapper.insert(version);

        // Update api status
        api.setStatus("PUBLISHED");
        api.setCurrentVersionId(version.getId());
        apiDefinitionMapper.updateById(api);

        // Broadcast route change
        try {
            stringRedisTemplate.convertAndSend("ds:route:channel", "PUBLISH:" + apiId);
        } catch (Exception e) {
            log.warn("Redis broadcast failed: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("versionId", version.getId());
        result.put("versionNo", versionNo);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> offline(Long apiId) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");
        if (!"PUBLISHED".equals(api.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATUS, "仅 PUBLISHED 状态可下线");
        }

        api.setStatus("OFFLINE");
        apiDefinitionMapper.updateById(api);

        try {
            stringRedisTemplate.convertAndSend("ds:route:channel", "OFFLINE:" + apiId);
        } catch (Exception e) {
            log.warn("Redis broadcast failed: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> rollback(Long apiId, RollbackReq req) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");

        ApiVersion version = apiVersionMapper.selectById(req.getVersionId());
        if (version == null || !version.getApiId().equals(apiId)) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "版本不存在");
        }

        try {
            Map<String, Object> snapshot = objectMapper.readValue(version.getSnapshot(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

            // Restore params
            apiParamMapper.delete(new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId));
            Object paramsObj = snapshot.get("params");
            if (paramsObj instanceof List) {
                List<Map<String, Object>> paramsList = (List<Map<String, Object>>) paramsObj;
                for (Map<String, Object> p : paramsList) {
                    ApiParam param = objectMapper.convertValue(p, ApiParam.class);
                    param.setId(null);
                    param.setCreatedAt(null);
                    param.setUpdatedAt(null);
                    apiParamMapper.insert(param);
                }
            }

            // Restore response fields
            apiResponseFieldMapper.delete(new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, apiId));
            Object fieldsObj = snapshot.get("responseFields");
            if (fieldsObj instanceof List) {
                List<Map<String, Object>> fieldsList = (List<Map<String, Object>>) fieldsObj;
                for (Map<String, Object> f : fieldsList) {
                    ApiResponseField field = objectMapper.convertValue(f, ApiResponseField.class);
                    field.setId(null);
                    field.setCreatedAt(null);
                    field.setUpdatedAt(null);
                    apiResponseFieldMapper.insert(field);
                }
            }

            // Restore API definition core fields
            Object apiObj = snapshot.get("api");
            if (apiObj instanceof Map) {
                Map<String, Object> apiMap = (Map<String, Object>) apiObj;
                if (apiMap.get("sqlTemplate") != null) api.setSqlTemplate(apiMap.get("sqlTemplate").toString());
                if (apiMap.get("datasourceId") instanceof Number)
                    api.setDatasourceId(((Number) apiMap.get("datasourceId")).longValue());
                if (apiMap.get("timeoutMs") instanceof Number)
                    api.setTimeoutMs(((Number) apiMap.get("timeoutMs")).intValue());
                if (apiMap.get("cacheEnable") instanceof Number)
                    api.setCacheEnable(((Number) apiMap.get("cacheEnable")).intValue());
                if (apiMap.get("cacheTtl") instanceof Number)
                    api.setCacheTtl(((Number) apiMap.get("cacheTtl")).intValue());
                else api.setCacheTtl(null);
                if (apiMap.get("defaultQpsLimit") instanceof Number)
                    api.setDefaultQpsLimit(((Number) apiMap.get("defaultQpsLimit")).intValue());
                if (apiMap.get("description") != null)
                    api.setDescription(apiMap.get("description").toString());
            }

            // 将当前 PUBLISHED 版本置为 HISTORY
            apiVersionMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ApiVersion>()
                            .eq(ApiVersion::getApiId, apiId)
                            .eq(ApiVersion::getStatus, "PUBLISHED")
                            .set(ApiVersion::getStatus, "HISTORY"));

            // 将目标版本置为 PUBLISHED
            version.setStatus("PUBLISHED");
            apiVersionMapper.updateById(version);

            api.setCurrentVersionId(version.getId());
            api.setStatus("PUBLISHED");
            apiDefinitionMapper.updateById(api);

            stringRedisTemplate.convertAndSend("ds:route:channel", "PUBLISH:" + apiId);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.SNAPSHOT_FAIL, "版本回滚失败: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("versionNo", version.getVersionNo());
        return result;
    }

    @Override
    public List<ApiVersionVO> listVersions(Long apiId) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        Long currentVersionId = api != null ? api.getCurrentVersionId() : null;

        List<ApiVersion> versions = apiVersionMapper.selectList(
                new LambdaQueryWrapper<ApiVersion>()
                        .eq(ApiVersion::getApiId, apiId)
                        .orderByDesc(ApiVersion::getId));
        return versions.stream().map(v -> toVO(v, currentVersionId)).collect(Collectors.toList());
    }

    private ApiVersionVO toVO(ApiVersion v, Long currentVersionId) {
        ApiVersionVO vo = new ApiVersionVO();
        vo.setId(v.getId());
        vo.setApiId(v.getApiId());
        vo.setVersionNo(v.getVersionNo());
        vo.setChangeLog(v.getChangeLog());
        vo.setStatus(v.getStatus());
        vo.setPublishedBy(v.getPublishedBy());
        vo.setPublishedByName("管理员");
        vo.setIsCurrent(v.getId().equals(currentVersionId));
        if (v.getPublishedAt() != null) vo.setPublishedAt(v.getPublishedAt().format(FMT));

        // 从快照中提取版本详情
        if (v.getSnapshot() != null) {
            try {
                Map<String, Object> snapshot = objectMapper.readValue(v.getSnapshot(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                // 参数摘要 & 数量
                Object paramsObj = snapshot.get("params");
                if (paramsObj instanceof List) {
                    List<Map<String, Object>> paramsList = (List<Map<String, Object>>) paramsObj;
                    vo.setParamCount(paramsList.size());
                    List<Map<String, Object>> summaryList = new ArrayList<>();
                    for (Map<String, Object> p : paramsList) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("name", p.getOrDefault("name", ""));
                        item.put("dataType", p.getOrDefault("dataType", ""));
                        item.put("required", p.getOrDefault("required", 0));
                        item.put("operator", p.getOrDefault("operator", ""));
                        item.put("defaultValue", p.getOrDefault("defaultValue", ""));
                        item.put("location", p.getOrDefault("location", ""));
                        summaryList.add(item);
                    }
                    vo.setParamSummary(summaryList);
                }

                // 响应字段数量
                Object fieldsObj = snapshot.get("responseFields");
                if (fieldsObj instanceof List) {
                    vo.setResponseFieldCount(((List<?>) fieldsObj).size());
                }

                // API 配置信息
                Object apiObj = snapshot.get("api");
                if (apiObj instanceof Map) {
                    Map<String, Object> apiMap = (Map<String, Object>) apiObj;
                    Object sql = apiMap.get("sqlTemplate");
                    if (sql != null) vo.setSqlTemplate(sql.toString());
                    if (apiMap.get("datasourceId") instanceof Number)
                        vo.setDatasourceId(((Number) apiMap.get("datasourceId")).longValue());
                    if (apiMap.get("timeoutMs") instanceof Number)
                        vo.setTimeoutMs(((Number) apiMap.get("timeoutMs")).intValue());
                    if (apiMap.get("cacheEnable") instanceof Number)
                        vo.setCacheEnable(((Number) apiMap.get("cacheEnable")).intValue() == 1);
                    if (apiMap.get("cacheTtl") instanceof Number)
                        vo.setCacheTtl(((Number) apiMap.get("cacheTtl")).intValue());
                    if (apiMap.get("defaultQpsLimit") instanceof Number)
                        vo.setDefaultQpsLimit(((Number) apiMap.get("defaultQpsLimit")).intValue());
                }
            } catch (Exception e) {
                log.warn("解析版本快照失败, versionId={}: {}", v.getId(), e.getMessage());
            }
        }
        return vo;
    }
}
