package com.isom.dataserver.module.gateway.engine;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BoundSqlResult {
    private String renderedSql;
    private List<Map<String, Object>> parameterMappings;
    private Map<String, Object> parameterObject;
    private List<Object> parameterValues = new ArrayList<>();
}
