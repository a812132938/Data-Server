package com.isom.dataserver.module.alert.service;

import com.isom.dataserver.common.page.PageResult;

import java.util.List;
import java.util.Map;

public interface AlertRuleService {
    Map<String, Object> create(Map<String, Object> req);
    Map<String, Object> update(Long id, Map<String, Object> req);
    void delete(Long id);
    PageResult<Map<String, Object>> list(Map<String, Object> query);
    Map<String, Object> detail(Long id);
    void enable(Long id);
    void disable(Long id);
}
