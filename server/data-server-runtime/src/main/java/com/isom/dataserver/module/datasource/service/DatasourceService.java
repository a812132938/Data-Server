package com.isom.dataserver.module.datasource.service;

import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.datasource.dto.DatasourceQuery;
import com.isom.dataserver.module.datasource.dto.DatasourceUpsertReq;
import com.isom.dataserver.module.datasource.vo.*;

import java.util.Map;

public interface DatasourceService {
    Map<String, Object> create(DatasourceUpsertReq req);
    Map<String, Object> update(Long id, DatasourceUpsertReq req);
    void delete(Long id);
    PageResult<DatasourceVO> list(DatasourceQuery query);
    DatasourceVO detail(Long id);
    TestResultVO testNew(DatasourceUpsertReq req);
    TestResultVO testExisting(Long id);
    SchemaVO schemas(Long id);
    DatasourceSummaryVO summary();
    void disable(Long id);
    void enable(Long id);
}
