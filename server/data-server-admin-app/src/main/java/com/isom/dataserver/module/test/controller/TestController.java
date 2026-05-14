package com.isom.dataserver.module.test.controller;

import com.isom.dataserver.common.page.PageQuery;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.test.dto.RunTestReq;
import com.isom.dataserver.module.test.dto.TestCaseReq;
import com.isom.dataserver.module.test.service.TestService;
import com.isom.dataserver.module.test.vo.RunTestResp;
import com.isom.dataserver.module.test.vo.TestCaseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Api(tags = "API测试")
@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @ApiOperation("执行API测试")
    @PostMapping("/api/v1/apis/{apiId}/test/run")
    public Result<RunTestResp> runTest(@ApiParam("API ID") @PathVariable Long apiId, @RequestBody RunTestReq req) {
        return Result.ok(testService.runTest(apiId, req));
    }

    @ApiOperation("查询测试用例列表")
    @GetMapping("/api/v1/apis/{apiId}/test-cases")
    public Result<PageResult<TestCaseVO>> listCases(@ApiParam("API ID") @PathVariable Long apiId, PageQuery query) {
        return Result.ok(testService.listCases(apiId, query));
    }

    @ApiOperation("创建测试用例")
    @PostMapping("/api/v1/apis/{apiId}/test-cases")
    public Result<Map<String, Object>> createCase(@ApiParam("API ID") @PathVariable Long apiId, @RequestBody TestCaseReq req) {
        return Result.ok(testService.createCase(apiId, req));
    }

    @ApiOperation("更新测试用例")
    @PutMapping("/api/v1/test-cases/{caseId}")
    public Result<Map<String, Object>> updateCase(@ApiParam("测试用例ID") @PathVariable Long caseId, @RequestBody TestCaseReq req) {
        return Result.ok(testService.updateCase(caseId, req));
    }

    @ApiOperation("删除测试用例")
    @DeleteMapping("/api/v1/test-cases/{caseId}")
    public Result<Map<String, Boolean>> deleteCase(@ApiParam("测试用例ID") @PathVariable Long caseId) {
        testService.deleteCase(caseId);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("运行所有测试用例（门禁测试）")
    @PostMapping("/api/v1/apis/{apiId}/test-cases/run-all")
    public Result<Map<String, Object>> gateTest(@ApiParam("API ID") @PathVariable Long apiId) {
        return Result.ok(testService.runAll(apiId));
    }
}
