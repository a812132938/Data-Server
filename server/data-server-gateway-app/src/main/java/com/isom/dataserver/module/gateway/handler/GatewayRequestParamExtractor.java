package com.isom.dataserver.module.gateway.handler;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class GatewayRequestParamExtractor {

    public Map<String, Object> extract(HttpServletRequest request, Map<String, Object> body) {
        Map<String, Object> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length == 1) {
                params.put(key, values[0]);
            } else {
                params.put(key, Arrays.asList(values));
            }
        });
        if (body != null) {
            params.putAll(body);
        }
        return params;
    }
}
