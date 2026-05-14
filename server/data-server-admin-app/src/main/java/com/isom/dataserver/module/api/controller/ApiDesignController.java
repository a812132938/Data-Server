package com.isom.dataserver.module.api.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.api.dto.QueryDesignRenderReq;
import com.isom.dataserver.module.api.dto.SqlPreviewReq;
import com.isom.dataserver.module.api.service.QueryDesignService;
import com.isom.dataserver.module.api.service.SqlPreviewService;
import com.isom.dataserver.module.api.vo.QueryDesignRenderResp;
import com.isom.dataserver.module.api.vo.SqlPreviewResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "API查询设计")
@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
public class ApiDesignController {

    private final QueryDesignService queryDesignService;
    private final SqlPreviewService sqlPreviewService;

    @ApiOperation("渲染多表查询设计")
    @PostMapping("/query-design/render")
    public Result<QueryDesignRenderResp> renderQueryDesign(@RequestBody QueryDesignRenderReq req) {
        return Result.ok(queryDesignService.render(req));
    }

    @ApiOperation("SQL预览")
    @PostMapping("/sql/preview")
    public Result<SqlPreviewResp> previewSql(@RequestBody SqlPreviewReq req) {
        return Result.ok(sqlPreviewService.preview(req));
    }
}
