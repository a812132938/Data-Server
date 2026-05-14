package com.isom.dataserver.module.api.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.api.dto.ApiQuery;
import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import com.isom.dataserver.module.api.dto.CloneReq;
import com.isom.dataserver.module.api.service.ApiDefinitionService;
import com.isom.dataserver.module.api.vo.ApiSummaryVO;
import com.isom.dataserver.module.api.vo.ApiVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Api(tags = "API定义管理")
@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
public class ApiDefinitionController {

    private final ApiDefinitionService apiDefinitionService;

    @ApiOperation("创建API")
    @PostMapping
    @AuditLog(action = "CREATE", targetType = "API")
    public Result<Map<String, Object>> create(@RequestBody ApiUpsertReq req) {
        return Result.ok(apiDefinitionService.create(req));
    }

    @ApiOperation("更新API")
    @PutMapping("/{id}")
    @AuditLog(action = "UPDATE", targetType = "API")
    public Result<Map<String, Object>> update(@ApiParam("API ID") @PathVariable Long id, @RequestBody ApiUpsertReq req) {
        return Result.ok(apiDefinitionService.update(id, req));
    }

    @ApiOperation("删除API")
    @DeleteMapping("/{id}")
    @AuditLog(action = "DELETE", targetType = "API")
    public Result<Map<String, Boolean>> delete(@ApiParam("API ID") @PathVariable Long id) {
        apiDefinitionService.delete(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("分页查询API列表")
    @GetMapping
    public Result<PageResult<ApiVO>> list(ApiQuery query) {
        return Result.ok(apiDefinitionService.list(query));
    }

    @ApiOperation("查询API详情")
    @GetMapping("/{id}")
    public Result<ApiVO> detail(@ApiParam("API ID") @PathVariable Long id) {
        return Result.ok(apiDefinitionService.detail(id));
    }

    @ApiOperation("克隆API")
    @PostMapping("/{id}/clone")
    @AuditLog(action = "CLONE", targetType = "API")
    public Result<Map<String, Object>> clone(@ApiParam("源API ID") @PathVariable Long id, @RequestBody CloneReq req) {
        return Result.ok(apiDefinitionService.clone(id, req));
    }

    @ApiOperation("获取API统计概览")
    @GetMapping("/summary")
    public Result<ApiSummaryVO> summary() {
        return Result.ok(apiDefinitionService.summary());
    }
}
