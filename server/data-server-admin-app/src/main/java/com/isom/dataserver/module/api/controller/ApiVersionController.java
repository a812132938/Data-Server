package com.isom.dataserver.module.api.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.api.dto.PublishReq;
import com.isom.dataserver.module.api.dto.RollbackReq;
import com.isom.dataserver.module.api.service.ApiVersionService;
import com.isom.dataserver.module.api.vo.ApiVersionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "API版本管理")
@RestController
@RequiredArgsConstructor
public class ApiVersionController {

    private final ApiVersionService apiVersionService;

    @ApiOperation("发布API版本")
    @PostMapping("/api/v1/apis/{apiId}/publish")
    @AuditLog(action = "PUBLISH", targetType = "API")
    public Result<Map<String, Object>> publish(@ApiParam("API ID") @PathVariable Long apiId, @RequestBody PublishReq req) {
        return Result.ok(apiVersionService.publish(apiId, req));
    }

    @ApiOperation("下线API")
    @PostMapping("/api/v1/apis/{apiId}/offline")
    @AuditLog(action = "OFFLINE", targetType = "API")
    public Result<Map<String, Object>> offline(@ApiParam("API ID") @PathVariable Long apiId) {
        return Result.ok(apiVersionService.offline(apiId));
    }

    @ApiOperation("回滚API版本")
    @PostMapping("/api/v1/apis/{apiId}/rollback")
    @AuditLog(action = "ROLLBACK", targetType = "API")
    public Result<Map<String, Object>> rollback(@ApiParam("API ID") @PathVariable Long apiId, @RequestBody RollbackReq req) {
        return Result.ok(apiVersionService.rollback(apiId, req));
    }

    @ApiOperation("查询API版本列表")
    @GetMapping("/api/v1/apis/{apiId}/versions")
    public Result<List<ApiVersionVO>> listVersions(@ApiParam("API ID") @PathVariable Long apiId) {
        return Result.ok(apiVersionService.listVersions(apiId));
    }
}
