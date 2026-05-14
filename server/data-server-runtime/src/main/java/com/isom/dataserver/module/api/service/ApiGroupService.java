package com.isom.dataserver.module.api.service;

import com.isom.dataserver.module.api.dto.ApiGroupReq;
import com.isom.dataserver.module.api.vo.ApiGroupTreeVO;

import java.util.List;
import java.util.Map;

public interface ApiGroupService {
    List<ApiGroupTreeVO> tree();
    Map<String, Object> create(ApiGroupReq req);
    Map<String, Object> update(Long id, ApiGroupReq req);
    void delete(Long id);
}
