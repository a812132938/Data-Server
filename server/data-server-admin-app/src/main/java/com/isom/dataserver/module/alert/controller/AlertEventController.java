package com.isom.dataserver.module.alert.controller;

import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.alert.service.AlertEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "告警事件")
@RestController
@RequestMapping("/api/v1/alert-events")
@RequiredArgsConstructor
public class AlertEventController {

    private final AlertEventService alertEventService;

    @ApiOperation("分页查询告警事件")
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("规则ID") @RequestParam(required = false) Long ruleId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> query = new HashMap<>();
        query.put("page", page);
        query.put("size", size);
        if (status != null) query.put("status", status);
        if (ruleId != null) query.put("ruleId", ruleId);
        return Result.ok(alertEventService.list(query));
    }

    @ApiOperation("最近告警事件")
    @GetMapping("/recent")
    public Result<Map<String, Object>> recent(
            @ApiParam("条数") @RequestParam(defaultValue = "20") Integer limit) {
        return Result.ok(alertEventService.recent(limit));
    }
}
