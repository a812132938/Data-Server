package com.isom.dataserver.module.gateway.chain;

import com.isom.dataserver.module.gateway.route.ApiRouteEntry;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GatewayContext {
    private ApiRouteEntry route;
    private Map<String, Object> requestParams;
    private String authMode;
    private Long appId;
    private Long userId;
    private List<Map<String, Object>> responseData;
    private String renderedSql;
    private boolean masked;
    private long startTime;
    private String errorMessage;
}
