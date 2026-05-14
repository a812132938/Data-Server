package com.isom.dataserver.module.gateway.log;

import com.isom.dataserver.module.log.entity.CallLog;
import com.isom.dataserver.module.log.mapper.CallLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncCallLogWriter {

    private final CallLogMapper callLogMapper;

    @Async
    public void write(Map<String, Object> logData) {
        try {
            CallLog entity = new CallLog();
            entity.setApiId(toLong(logData.get("apiId")));
            entity.setApiPath((String) logData.get("apiPath"));
            entity.setAppId(toLong(logData.get("appId")));
            entity.setClientIp((String) logData.get("clientIp"));
            entity.setCostMs(toInt(logData.get("costMs")));
            entity.setHttpStatus(toShort(logData.get("httpStatus")));
            entity.setBizCode(toInt(logData.get("bizCode")));
            entity.setAuthType((String) logData.get("authType"));
            entity.setCacheHit(Boolean.TRUE.equals(logData.get("cacheHit")));
            entity.setResponseSize(toInt(logData.get("responseSize")));
            entity.setRecordCount(toInt(logData.get("recordCount")));
            entity.setErrorMsg((String) logData.get("errorMsg"));
            entity.setStatDate(LocalDate.now());
            callLogMapper.insert(entity);
        } catch (Exception e) {
            log.error("Failed to write call log", e);
        }
    }

    private Long toLong(Object obj) {
        return obj instanceof Number ? ((Number) obj).longValue() : null;
    }

    private Integer toInt(Object obj) {
        return obj instanceof Number ? ((Number) obj).intValue() : null;
    }

    private Short toShort(Object obj) {
        return obj instanceof Number ? ((Number) obj).shortValue() : null;
    }
}
