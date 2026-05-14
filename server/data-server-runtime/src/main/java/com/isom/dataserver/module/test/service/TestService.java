package com.isom.dataserver.module.test.service;

import com.isom.dataserver.common.page.PageQuery;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.test.dto.RunTestReq;
import com.isom.dataserver.module.test.dto.TestCaseReq;
import com.isom.dataserver.module.test.vo.RunTestResp;
import com.isom.dataserver.module.test.vo.TestCaseVO;

import java.util.Map;

public interface TestService {
    RunTestResp runTest(Long apiId, RunTestReq req);
    PageResult<TestCaseVO> listCases(Long apiId, PageQuery query);
    Map<String, Object> createCase(Long apiId, TestCaseReq req);
    Map<String, Object> updateCase(Long caseId, TestCaseReq req);
    void deleteCase(Long caseId);
    Map<String, Object> runAll(Long apiId);
}
