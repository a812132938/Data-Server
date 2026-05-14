package com.isom.dataserver.module.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.mapper.AppMapper;
import com.isom.dataserver.module.log.entity.CallLog;
import com.isom.dataserver.module.log.entity.CallStatDaily;
import com.isom.dataserver.module.log.mapper.CallLogMapper;
import com.isom.dataserver.module.log.mapper.CallStatDailyMapper;
import com.isom.dataserver.module.log.service.CallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallLogServiceImpl implements CallLogService {

    private final CallLogMapper callLogMapper;
    private final CallStatDailyMapper callStatDailyMapper;
    private final ApiDefinitionMapper apiDefinitionMapper;
    private final AppMapper appMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<Map<String, Object>> list(Map<String, Object> query) {
        int page = query.get("page") != null ? Integer.parseInt(query.get("page").toString()) : 1;
        int size = query.get("size") != null ? Integer.parseInt(query.get("size").toString()) : 10;

        LambdaQueryWrapper<CallLog> wrapper = new LambdaQueryWrapper<>();
        if (query.get("apiId") != null) {
            wrapper.eq(CallLog::getApiId, Long.parseLong(query.get("apiId").toString()));
        }
        if (query.get("appId") != null) {
            wrapper.eq(CallLog::getAppId, Long.parseLong(query.get("appId").toString()));
        }
        if (query.get("httpStatus") != null) {
            wrapper.eq(CallLog::getHttpStatus, Short.parseShort(query.get("httpStatus").toString()));
        }
        wrapper.orderByDesc(CallLog::getCreatedAt);

        Page<CallLog> p = callLogMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量查询 API 名称和应用名称
        Set<Long> apiIds = p.getRecords().stream().map(CallLog::getApiId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> appIds = p.getRecords().stream().map(CallLog::getAppId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> apiNameMap = apiIds.isEmpty() ? Collections.emptyMap()
                : apiDefinitionMapper.selectBatchIds(apiIds).stream().collect(Collectors.toMap(ApiDefinition::getId, ApiDefinition::getName));
        Map<Long, String> appNameMap = appIds.isEmpty() ? Collections.emptyMap()
                : appMapper.selectBatchIds(appIds).stream().collect(Collectors.toMap(App::getId, App::getName));

        List<Map<String, Object>> records = p.getRecords().stream().map(l -> {
            Map<String, Object> m = logToMap(l);
            m.put("apiName", apiNameMap.getOrDefault(l.getApiId(), ""));
            m.put("appName", appNameMap.getOrDefault(l.getAppId(), ""));
            return m;
        }).collect(Collectors.toList());
        return PageResult.of(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    @Override
    public Map<String, Object> detail(Long id) {
        CallLog log = callLogMapper.selectById(id);
        if (log == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "日志不存在");
        Map<String, Object> m = logToMap(log);
        if (log.getApiId() != null) {
            ApiDefinition api = apiDefinitionMapper.selectById(log.getApiId());
            m.put("apiName", api != null ? api.getName() : "");
        }
        if (log.getAppId() != null) {
            App app = appMapper.selectById(log.getAppId());
            m.put("appName", app != null ? app.getName() : "");
        }
        return m;
    }

    @Override
    public Map<String, Object> stats(Long apiId, String startDate, String endDate) {
        LambdaQueryWrapper<CallStatDaily> wrapper = new LambdaQueryWrapper<>();
        if (apiId != null) wrapper.eq(CallStatDaily::getApiId, apiId);
        if (startDate != null) wrapper.ge(CallStatDaily::getStatDate, LocalDate.parse(startDate));
        if (endDate != null) wrapper.le(CallStatDaily::getStatDate, LocalDate.parse(endDate));
        wrapper.orderByAsc(CallStatDaily::getStatDate);

        List<CallStatDaily> stats = callStatDailyMapper.selectList(wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", stats);
        return result;
    }

    @Override
    @Async
    public void writeLog(Map<String, Object> logData) {
        CallLog entity = new CallLog();
        entity.setApiId(logData.get("apiId") != null ? Long.parseLong(logData.get("apiId").toString()) : null);
        entity.setApiPath((String) logData.get("apiPath"));
        entity.setAppId(logData.get("appId") != null ? Long.parseLong(logData.get("appId").toString()) : null);
        entity.setClientIp((String) logData.get("clientIp"));
        entity.setTraceId((String) logData.get("traceId"));
        entity.setRequestParams((String) logData.get("requestParams"));
        entity.setResponseSize(logData.get("responseSize") != null ? Integer.parseInt(logData.get("responseSize").toString()) : 0);
        entity.setRecordCount(logData.get("recordCount") != null ? Integer.parseInt(logData.get("recordCount").toString()) : 0);
        entity.setHttpStatus(logData.get("httpStatus") != null ? Short.parseShort(logData.get("httpStatus").toString()) : (short) 200);
        entity.setBizCode(logData.get("bizCode") != null ? Integer.parseInt(logData.get("bizCode").toString()) : 0);
        entity.setCostMs(logData.get("costMs") != null ? Integer.parseInt(logData.get("costMs").toString()) : 0);
        entity.setErrorMsg((String) logData.get("errorMsg"));
        entity.setAuthType((String) logData.get("authType"));
        entity.setCacheHit(logData.get("cacheHit") != null ? (Boolean) logData.get("cacheHit") : false);
        entity.setStatDate(LocalDate.now());
        callLogMapper.insert(entity);
    }

    private Map<String, Object> logToMap(CallLog log) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", log.getId());
        m.put("traceId", log.getTraceId() != null ? log.getTraceId() : "");
        m.put("apiId", log.getApiId());
        m.put("apiPath", log.getApiPath() != null ? log.getApiPath() : "");
        m.put("appId", log.getAppId());
        m.put("clientIp", log.getClientIp() != null ? log.getClientIp() : "");
        m.put("requestParams", log.getRequestParams() != null ? log.getRequestParams() : "");
        m.put("responseSize", log.getResponseSize() != null ? log.getResponseSize() : 0);
        m.put("recordCount", log.getRecordCount() != null ? log.getRecordCount() : 0);
        m.put("httpStatus", log.getHttpStatus() != null ? log.getHttpStatus() : 0);
        m.put("bizCode", log.getBizCode() != null ? log.getBizCode() : 0);
        m.put("costMs", log.getCostMs() != null ? log.getCostMs() : 0);
        m.put("errorMsg", log.getErrorMsg() != null ? log.getErrorMsg() : "");
        m.put("authType", log.getAuthType() != null ? log.getAuthType() : "");
        m.put("cacheHit", log.getCacheHit() != null ? log.getCacheHit() : false);
        m.put("statDate", log.getStatDate() != null ? log.getStatDate().toString() : "");
        m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().format(FMT) : "");
        return m;
    }
}
