package com.isom.dataserver.module.alert.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.alert.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "通知消息")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @ApiOperation("站内通知列表")
    @GetMapping("/insite")
    public Result<Map<String, Object>> insite(
            @ApiParam("状态过滤") @RequestParam(required = false) String status,
            @ApiParam("通知类型") @RequestParam(required = false) String type,
            @ApiParam("条数") @RequestParam(defaultValue = "20") Integer limit) {
        return Result.ok(notificationService.insite(status, type, limit));
    }

    @ApiOperation("标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<Map<String, Object>> markRead(@ApiParam("通知ID") @PathVariable Long id) {
        return Result.ok(notificationService.markRead(id));
    }

    @ApiOperation("标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Map<String, Object>> markAllRead() {
        return Result.ok(notificationService.markAllRead());
    }
}
