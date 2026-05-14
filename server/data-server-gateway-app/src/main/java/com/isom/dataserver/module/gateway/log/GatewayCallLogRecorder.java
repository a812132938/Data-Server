package com.isom.dataserver.module.gateway.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isom.dataserver.module.gateway.chain.GatewayContext;
import com.isom.dataserver.module.gateway.route.ApiRouteEntry;
import com.isom.dataserver.module.log.service.CallLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GatewayCallLogRecorder {

    private static final String APP_CODE_PARAM = "app_code";

    private final CallLogService callLogService;
    private final ObjectMapper objectMapper;

    public void recordSuccess(HttpServletRequest request, GatewayContext context) {
        ApiRouteEntry route = context.getRoute();
        Map<String, Object> logData = new HashMap<>();
        logData.put("traceId", MDC.get("traceId"));
        logData.put("apiId", route.getApiId());
        logData.put("apiPath", route.getPath());
        logData.put("appId", context.getAppId());
        logData.put("clientIp", request.getRemoteAddr());
        logData.put("requestParams", serializeParams(context.getRequestParams()));
        logData.put("costMs", System.currentTimeMillis() - context.getStartTime());
        logData.put("httpStatus", 200);
        logData.put("bizCode", 0);
        logData.put("authType", context.getAuthMode());
        logData.put("cacheHit", false);
        logData.put("responseSize", rowCount(context));
        logData.put("recordCount", rowCount(context));
        callLogService.writeLog(logData);
    }

    private String serializeParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }
        try {
            Map<String, Object> safeParams = new HashMap<>(params);
            safeParams.remove(APP_CODE_PARAM);
            return objectMapper.writeValueAsString(safeParams);
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private int rowCount(GatewayContext context) {
        return context.getResponseData() != null ? context.getResponseData().size() : 0;
    }
}
