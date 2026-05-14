package com.isom.dataserver.module.app.service;

import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.app.dto.AppCreateReq;
import com.isom.dataserver.module.app.dto.AppQuery;
import com.isom.dataserver.module.app.dto.AppUpdateReq;
import com.isom.dataserver.module.app.vo.AppCreateVO;
import com.isom.dataserver.module.app.vo.AppSummaryVO;
import com.isom.dataserver.module.app.vo.AppVO;

import java.util.Map;

public interface AppService {
    AppCreateVO create(AppCreateReq req);
    Map<String, Object> update(Long id, AppUpdateReq req);
    void delete(Long id);
    PageResult<AppVO> list(AppQuery query);
    AppVO detail(Long id);
    Map<String, Object> resetSecret(Long id);
    void disable(Long id);
    void enable(Long id);
    AppSummaryVO summary();
}
