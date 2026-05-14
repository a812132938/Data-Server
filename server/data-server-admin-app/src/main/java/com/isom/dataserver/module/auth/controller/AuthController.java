package com.isom.dataserver.module.auth.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.page.PageQuery;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.auth.service.AuthService;
import com.isom.dataserver.module.auth.vo.AuthVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Api(tags = "授权管理")
@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("按应用查询授权列表")
    @GetMapping("/by-app/{appId}")
    public Result<PageResult<AuthVO>> listByApp(@ApiParam("应用ID") @PathVariable Long appId, PageQuery query) {
        return Result.ok(authService.listByApp(appId, query));
    }

    @ApiOperation("按API查询授权列表")
    @GetMapping("/by-api/{apiId}")
    public Result<PageResult<AuthVO>> listByApi(@ApiParam("API ID") @PathVariable Long apiId, PageQuery query) {
        return Result.ok(authService.listByApi(apiId, query));
    }

    @ApiOperation("撤销授权")
    @PostMapping("/{authId}/revoke")
    @AuditLog(action = "REVOKE", targetType = "AUTH")
    public Result<Map<String, Boolean>> revoke(@ApiParam("授权ID") @PathVariable Long authId) {
        authService.revoke(authId);
        return Result.ok(Collections.singletonMap("success", true));
    }
}
