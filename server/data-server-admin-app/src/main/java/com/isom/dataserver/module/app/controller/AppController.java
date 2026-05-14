package com.isom.dataserver.module.app.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.app.dto.AppCreateReq;
import com.isom.dataserver.module.app.dto.AppQuery;
import com.isom.dataserver.module.app.dto.AppUpdateReq;
import com.isom.dataserver.module.app.service.AppService;
import com.isom.dataserver.module.app.vo.AppCreateVO;
import com.isom.dataserver.module.app.vo.AppSummaryVO;
import com.isom.dataserver.module.app.vo.AppVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Api(tags = "应用管理")
@RestController
@RequestMapping("/api/v1/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @ApiOperation("创建应用")
    @PostMapping
    @AuditLog(action = "CREATE", targetType = "APP")
    public Result<AppCreateVO> create(@RequestBody AppCreateReq req) {
        return Result.ok(appService.create(req));
    }

    @ApiOperation("更新应用")
    @PutMapping("/{id}")
    @AuditLog(action = "UPDATE", targetType = "APP")
    public Result<Map<String, Object>> update(@ApiParam("应用ID") @PathVariable Long id, @RequestBody AppUpdateReq req) {
        return Result.ok(appService.update(id, req));
    }

    @ApiOperation("删除应用")
    @DeleteMapping("/{id}")
    @AuditLog(action = "DELETE", targetType = "APP")
    public Result<Map<String, Boolean>> delete(@ApiParam("应用ID") @PathVariable Long id) {
        appService.delete(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("分页查询应用列表")
    @GetMapping
    public Result<PageResult<AppVO>> list(AppQuery query) {
        return Result.ok(appService.list(query));
    }

    @ApiOperation("查询应用详情")
    @GetMapping("/{id}")
    public Result<AppVO> detail(@ApiParam("应用ID") @PathVariable Long id) {
        return Result.ok(appService.detail(id));
    }

    @ApiOperation("重置应用密钥")
    @PostMapping("/{id}/reset-secret")
    @AuditLog(action = "RESET_SECRET", targetType = "APP")
    public Result<Map<String, Object>> resetSecret(@ApiParam("应用ID") @PathVariable Long id) {
        return Result.ok(appService.resetSecret(id));
    }

    @ApiOperation("禁用应用")
    @PostMapping("/{id}/disable")
    @AuditLog(action = "DISABLE", targetType = "APP")
    public Result<Map<String, Boolean>> disable(@ApiParam("应用ID") @PathVariable Long id) {
        appService.disable(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("启用应用")
    @PostMapping("/{id}/enable")
    @AuditLog(action = "ENABLE", targetType = "APP")
    public Result<Map<String, Boolean>> enable(@ApiParam("应用ID") @PathVariable Long id) {
        appService.enable(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("获取应用统计概览")
    @GetMapping("/summary")
    public Result<AppSummaryVO> summary() {
        return Result.ok(appService.summary());
    }
}
