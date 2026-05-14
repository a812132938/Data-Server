package com.isom.dataserver.module.doc.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.doc.service.DocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "API文档")
@RestController
@RequestMapping("/api/v1/docs")
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    @ApiOperation("获取API文档树")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        return Result.ok(docService.tree());
    }

    @ApiOperation("查询API文档详情")
    @GetMapping("/apis/{apiId}")
    public Result<Map<String, Object>> detail(@ApiParam("API ID") @PathVariable Long apiId) {
        return Result.ok(docService.detail(apiId));
    }

    @ApiOperation("在线调试API")
    @PostMapping("/apis/{apiId}/try")
    public Result<Map<String, Object>> tryApi(@ApiParam("API ID") @PathVariable Long apiId, @RequestBody(required = false) Map<String, Object> params) {
        return Result.ok(docService.tryApi(apiId, params != null ? params : new java.util.HashMap<>()));
    }
}
