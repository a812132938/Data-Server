package com.isom.dataserver.module.api.service;

import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.api.dto.ApiQuery;
import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import com.isom.dataserver.module.api.dto.CloneReq;
import com.isom.dataserver.module.api.vo.ApiSummaryVO;
import com.isom.dataserver.module.api.vo.ApiVO;

import java.util.Map;

public interface ApiDefinitionService {
    Map<String, Object> create(ApiUpsertReq req);
    Map<String, Object> update(Long id, ApiUpsertReq req);
    void delete(Long id);
    PageResult<ApiVO> list(ApiQuery query);
    ApiVO detail(Long id);
    Map<String, Object> clone(Long id, CloneReq req);
    ApiSummaryVO summary();
}
