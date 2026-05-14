package com.isom.dataserver.module.api.service;

import com.isom.dataserver.module.api.vo.ParseParamsResp;

import java.util.List;
import java.util.Map;

public interface SqlParserService {
    ParseParamsResp parse(Long apiId, String sqlTemplate);
    List<Map<String, Object>> listParams(Long apiId);
    Map<String, Object> batchSave(Long apiId, List<Map<String, Object>> params);
}
