package com.isom.dataserver.module.dashboard.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.dashboard.service.DashboardService;
import com.isom.dataserver.module.dashboard.vo.StatusDistVO;
import com.isom.dataserver.module.dashboard.vo.TopRankVO;
import com.isom.dataserver.module.dashboard.vo.TrendVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "仪表盘")
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @ApiOperation("查询调用趋势")
    @GetMapping("/api/v1/dashboard/trend")
    public Result<TrendVO> trend(
            @ApiParam("开始日期（yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期（yyyy-MM-dd）") @RequestParam(required = false) String endDate) {
        return Result.ok(dashboardService.trend(startDate, endDate));
    }

    @ApiOperation("查询状态分布")
    @GetMapping("/api/v1/dashboard/status-dist")
    public Result<StatusDistVO> statusDist() {
        return Result.ok(dashboardService.statusDist());
    }

    @ApiOperation("查询调用量TOP API排行")
    @GetMapping("/api/v1/dashboard/top-api")
    public Result<TopRankVO> topApi(@ApiParam("排行数量") @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.topApi(limit));
    }

    @ApiOperation("查询调用量TOP应用排行")
    @GetMapping("/api/v1/dashboard/top-app")
    public Result<TopRankVO> topApp(@ApiParam("排行数量") @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dashboardService.topApp(limit));
    }

    @ApiOperation("查询总览数据")
    @GetMapping("/api/v1/stats/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(dashboardService.overview());
    }
}
