package com.isom.dataserver.module.alert.service;

import com.isom.dataserver.common.page.PageResult;

import java.util.List;
import java.util.Map;

public interface AlertEventService {
    PageResult<Map<String, Object>> list(Map<String, Object> query);
    Map<String, Object> recent(Integer limit);
}
