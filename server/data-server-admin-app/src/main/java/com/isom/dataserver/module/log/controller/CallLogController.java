package com.isom.dataserver.module.log.controller;

import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.log.service.CallLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "调用日志")
@RestController
@RequiredArgsConstructor
public class CallLogController {

    private final CallLogService callLogService;

    @ApiOperation("分页查询调用日志")
    @GetMapping("/api/v1/logs")
    public Result<PageResult<Map<String, Object>>> list(
            @ApiParam("API ID") @RequestParam(required = false) Long apiId,
            @ApiParam("应用ID") @RequestParam(required = false) Long appId,
            @ApiParam("状态（success/fail）") @RequestParam(required = false) String status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> query = new HashMap<>();
        if (apiId != null) query.put("apiId", apiId);
        if (appId != null) query.put("appId", appId);
        if (status != null) query.put("status", status);
        query.put("page", page);
        query.put("size", size);
        return Result.ok(callLogService.list(query));
    }

    @ApiOperation("查询调用日志详情")
    @GetMapping("/api/v1/logs/{id}")
    public Result<Map<String, Object>> detail(@ApiParam("日志ID") @PathVariable Long id) {
        return Result.ok(callLogService.detail(id));
    }

    @ApiOperation("查询调用统计数据")
    @GetMapping("/api/v1/logs/stats")
    public Result<Map<String, Object>> stats(
            @ApiParam("API ID") @RequestParam(required = false) Long apiId,
            @ApiParam("开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate) {
        return Result.ok(callLogService.stats(apiId, startDate, endDate));
    }
}
