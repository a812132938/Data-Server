package com.isom.dataserver.module.log.service;

import com.isom.dataserver.common.page.PageResult;

import java.util.Map;

public interface CallLogService {
    PageResult<Map<String, Object>> list(Map<String, Object> query);
    Map<String, Object> detail(Long id);
    Map<String, Object> stats(Long apiId, String startDate, String endDate);
    void writeLog(Map<String, Object> logData);
}
