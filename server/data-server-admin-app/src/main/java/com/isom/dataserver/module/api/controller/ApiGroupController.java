package com.isom.dataserver.module.api.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.api.dto.ApiGroupReq;
import com.isom.dataserver.module.api.service.ApiGroupService;
import com.isom.dataserver.module.api.vo.ApiGroupTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(tags = "API分组管理")
@RestController
@RequestMapping("/api/v1/api-groups")
@RequiredArgsConstructor
public class ApiGroupController {

    private final ApiGroupService apiGroupService;

    @ApiOperation("获取API分组树")
    @GetMapping
    public Result<List<ApiGroupTreeVO>> tree() {
        return Result.ok(apiGroupService.tree());
    }

    @ApiOperation("创建API分组")
    @PostMapping
    @AuditLog(action = "CREATE", targetType = "API_GROUP")
    public Result<Map<String, Object>> create(@RequestBody ApiGroupReq req) {
        return Result.ok(apiGroupService.create(req));
    }

    @ApiOperation("更新API分组")
    @PutMapping("/{id}")
    @AuditLog(action = "UPDATE", targetType = "API_GROUP")
    public Result<Map<String, Object>> update(@ApiParam("分组ID") @PathVariable Long id, @RequestBody ApiGroupReq req) {
        return Result.ok(apiGroupService.update(id, req));
    }

    @ApiOperation("删除API分组")
    @DeleteMapping("/{id}")
    @AuditLog(action = "DELETE", targetType = "API_GROUP")
    public Result<Map<String, Boolean>> delete(@ApiParam("分组ID") @PathVariable Long id) {
        apiGroupService.delete(id);
        return Result.ok(Collections.singletonMap("success", true));
    }
}
