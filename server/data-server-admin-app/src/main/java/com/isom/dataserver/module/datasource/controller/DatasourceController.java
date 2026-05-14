package com.isom.dataserver.module.datasource.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.datasource.dto.DatasourceQuery;
import com.isom.dataserver.module.datasource.dto.DatasourceUpsertReq;
import com.isom.dataserver.module.datasource.service.DatasourceService;
import com.isom.dataserver.module.datasource.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@Api(tags = "数据源管理")
@RestController
@RequestMapping("/api/v1/datasources")
@RequiredArgsConstructor
public class DatasourceController {

    private final DatasourceService datasourceService;

    @ApiOperation("创建数据源")
    @PostMapping
    @AuditLog(action = "CREATE", targetType = "DATASOURCE")
    public Result<Map<String, Object>> create(@RequestBody @Valid DatasourceUpsertReq req) {
        return Result.ok(datasourceService.create(req));
    }

    @ApiOperation("更新数据源")
    @PutMapping("/{id}")
    @AuditLog(action = "UPDATE", targetType = "DATASOURCE")
    public Result<Map<String, Object>> update(@ApiParam("数据源ID") @PathVariable Long id, @RequestBody @Valid DatasourceUpsertReq req) {
        return Result.ok(datasourceService.update(id, req));
    }

    @ApiOperation("删除数据源")
    @DeleteMapping("/{id}")
    @AuditLog(action = "DELETE", targetType = "DATASOURCE")
    public Result<Map<String, Boolean>> delete(@ApiParam("数据源ID") @PathVariable Long id) {
        datasourceService.delete(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("分页查询数据源列表")
    @GetMapping
    public Result<PageResult<DatasourceVO>> list(DatasourceQuery query) {
        return Result.ok(datasourceService.list(query));
    }

    @ApiOperation("查询数据源详情")
    @GetMapping("/{id}")
    public Result<DatasourceVO> detail(@ApiParam("数据源ID") @PathVariable Long id) {
        return Result.ok(datasourceService.detail(id));
    }

    @ApiOperation("测试新数据源连接")
    @PostMapping("/test")
    public Result<TestResultVO> testNew(@RequestBody DatasourceUpsertReq req) {
        return Result.ok(datasourceService.testNew(req));
    }

    @ApiOperation("测试已有数据源连接")
    @PostMapping("/{id}/test")
    public Result<TestResultVO> testExisting(@ApiParam("数据源ID") @PathVariable Long id) {
        return Result.ok(datasourceService.testExisting(id));
    }

    @ApiOperation("获取数据源的库表结构")
    @GetMapping("/{id}/schemas")
    public Result<SchemaVO> schemas(@ApiParam("数据源ID") @PathVariable Long id) {
        return Result.ok(datasourceService.schemas(id));
    }

    @ApiOperation("获取数据源统计概览")
    @GetMapping("/summary")
    public Result<DatasourceSummaryVO> summary() {
        return Result.ok(datasourceService.summary());
    }

    @ApiOperation("禁用数据源")
    @PostMapping("/{id}/disable")
    @AuditLog(action = "DISABLE", targetType = "DATASOURCE")
    public Result<Map<String, Boolean>> disable(@ApiParam("数据源ID") @PathVariable Long id) {
        datasourceService.disable(id);
        return Result.ok(Collections.singletonMap("success", true));
    }

    @ApiOperation("启用数据源")
    @PostMapping("/{id}/enable")
    @AuditLog(action = "ENABLE", targetType = "DATASOURCE")
    public Result<Map<String, Boolean>> enable(@ApiParam("数据源ID") @PathVariable Long id) {
        datasourceService.enable(id);
        return Result.ok(Collections.singletonMap("success", true));
    }
}
