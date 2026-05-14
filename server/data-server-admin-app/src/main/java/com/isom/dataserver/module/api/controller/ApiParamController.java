package com.isom.dataserver.module.api.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.api.dto.ParseParamsReq;
import com.isom.dataserver.module.api.service.SqlParserService;
import com.isom.dataserver.module.api.vo.ParseParamsResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "API参数管理")
@RestController
@RequiredArgsConstructor
public class ApiParamController {

    private final SqlParserService sqlParserService;

    @ApiOperation("解析SQL模板中的参数")
    @PostMapping("/api/v1/apis/{id}/params/parse")
    public Result<ParseParamsResp> parse(@ApiParam("API ID") @PathVariable Long id, @RequestBody ParseParamsReq req) {
        return Result.ok(sqlParserService.parse(id, req.getSqlTemplate()));
    }

    @ApiOperation("查询API的参数列表")
    @GetMapping("/api/v1/apis/{id}/params")
    public Result<Map<String, Object>> listParams(@ApiParam("API ID") @PathVariable Long id) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", sqlParserService.listParams(id));
        return Result.ok(result);
    }

    @ApiOperation("批量保存API参数")
    @PutMapping("/api/v1/apis/{id}/params")
    public Result<Map<String, Object>> batchSave(@ApiParam("API ID") @PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> params = (List<Map<String, Object>>) body.get("params");
        return Result.ok(sqlParserService.batchSave(id, params));
    }
}
