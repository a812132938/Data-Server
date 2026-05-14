package com.isom.dataserver.module.doc.service;

import java.util.List;
import java.util.Map;

public interface DocService {
    List<Map<String, Object>> tree();
    Map<String, Object> detail(Long apiId);
    Map<String, Object> tryApi(Long apiId, Map<String, Object> params);
}
