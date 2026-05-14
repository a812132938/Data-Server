package com.isom.dataserver.module.api.service;

import com.isom.dataserver.module.api.dto.PublishReq;
import com.isom.dataserver.module.api.dto.RollbackReq;
import com.isom.dataserver.module.api.vo.ApiVersionVO;

import java.util.List;
import java.util.Map;

public interface ApiVersionService {
    Map<String, Object> publish(Long apiId, PublishReq req);
    Map<String, Object> offline(Long apiId);
    Map<String, Object> rollback(Long apiId, RollbackReq req);
    List<ApiVersionVO> listVersions(Long apiId);
}
