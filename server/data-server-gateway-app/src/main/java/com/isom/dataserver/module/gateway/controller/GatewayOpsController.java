package com.isom.dataserver.module.gateway.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.gateway.route.ApiRouteEntry;
import com.isom.dataserver.module.gateway.route.RouteLoader;
import com.isom.dataserver.module.gateway.route.RouteTable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Api(tags = "网关运行时运维")
@RestController
@RequestMapping("/internal/gateway")
@RequiredArgsConstructor
public class GatewayOpsController {

    private final RouteTable routeTable;
    private final RouteLoader routeLoader;

    @ApiOperation("查询路由表状态")
    @GetMapping("/routes/status")
    public Result<RouteTable.RouteTableStatus> status() {
        return Result.ok(routeTable.status());
    }

    @ApiOperation("查询内存路由")
    @GetMapping("/routes")
    public Result<Collection<ApiRouteEntry>> routes() {
        return Result.ok(routeTable.all());
    }

    @ApiOperation("手动刷新路由")
    @PostMapping("/routes/reload")
    public Result<Map<String, Boolean>> reload() {
        routeLoader.reload();
        return Result.ok(Collections.singletonMap("success", routeTable.status().isLastReloadSuccess()));
    }
}
