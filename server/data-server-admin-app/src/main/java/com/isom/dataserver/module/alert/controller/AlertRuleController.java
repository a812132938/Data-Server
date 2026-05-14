package com.isom.dataserver.module.alert.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.alert.service.AlertRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "告警规则")
@RestController
@RequestMapping("/api/v1/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @ApiOperation("创建告警规则")
    @PostMapping
    @AuditLog(action = "CREATE", targetType = "ALERT_RULE")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        return Result.ok(alertRuleService.create(req));
    }

    @ApiOperation("更新告警规则")
    @PutMapping("/{id}")
    @AuditLog(action = "UPDATE", targetType = "ALERT_RULE")
    public Result<Map<String, Object>> update(@ApiParam("规则ID") @PathVariable Long id, @RequestBody Map<String, Object> req) {
        return Result.ok(alertRuleService.update(id, req));
    }

    @ApiOperation("删除告警规则")
    @DeleteMapping("/{id}")
    @AuditLog(action = "DELETE", targetType = "ALERT_RULE")
    public Result<Map<String, Boolean>> delete(@ApiParam("规则ID") @PathVariable Long id) {
        alertRuleService.delete(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("分页查询告警规则列表")
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @ApiParam("目标类型") @RequestParam(required = false) String targetType,
            @ApiParam("指标") @RequestParam(required = false) String metric,
            @ApiParam("是否启用") @RequestParam(required = false) Boolean enabled,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> query = new HashMap<>();
        query.put("page", page);
        query.put("size", size);
        if (targetType != null) query.put("targetType", targetType);
        if (metric != null) query.put("metric", metric);
        if (enabled != null) query.put("enabled", enabled.toString());
        return Result.ok(alertRuleService.list(query));
    }

    @ApiOperation("查询告警规则详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@ApiParam("规则ID") @PathVariable Long id) {
        return Result.ok(alertRuleService.detail(id));
    }

    @ApiOperation("启用告警规则")
    @PostMapping("/{id}/enable")
    public Result<Map<String, Boolean>> enable(@ApiParam("规则ID") @PathVariable Long id) {
        alertRuleService.enable(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("禁用告警规则")
    @PostMapping("/{id}/disable")
    public Result<Map<String, Boolean>> disable(@ApiParam("规则ID") @PathVariable Long id) {
        alertRuleService.disable(id);
        return Result.ok(Collections.singletonMap("success", true));
    }
}
