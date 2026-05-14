package com.isom.dataserver.module.gateway.route;

import lombok.Data;

@Data
public class ApiRouteEntry {
    private Long apiId;
    private String path;
    private String method;
    private Long datasourceId;
    private String sqlTemplate;
    private Integer timeoutMs;
    private Integer cacheEnable;
    private Integer cacheTtl;
    private Integer defaultQpsLimit;
    private String status;
}
