package com.isom.dataserver.module.auth.controller;

import com.isom.dataserver.common.audit.AuditLog;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.auth.dto.ApprovalActionReq;
import com.isom.dataserver.module.auth.dto.ApprovalCreateReq;
import com.isom.dataserver.module.auth.dto.ApprovalQuery;
import com.isom.dataserver.module.auth.service.ApprovalService;
import com.isom.dataserver.module.auth.vo.ApprovalSummaryVO;
import com.isom.dataserver.module.auth.vo.ApprovalVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "审批管理")
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @ApiOperation("提交审批申请")
    @PostMapping
    @AuditLog(action = "SUBMIT", targetType = "APPROVAL")
    public Result<Map<String, Object>> submit(@RequestBody ApprovalCreateReq req) {
        return Result.ok(approvalService.submit(req));
    }

    @ApiOperation("处理审批（通过/拒绝）")
    @PostMapping("/{id}/action")
    @AuditLog(action = "ACTION", targetType = "APPROVAL")
    public Result<Map<String, Object>> action(@ApiParam("审批ID") @PathVariable Long id, @RequestBody ApprovalActionReq req) {
        return Result.ok(approvalService.action(id, req));
    }

    @ApiOperation("分页查询审批列表")
    @GetMapping
    public Result<PageResult<ApprovalVO>> list(ApprovalQuery query) {
        return Result.ok(approvalService.list(query));
    }

    @ApiOperation("查询审批详情")
    @GetMapping("/{id}")
    public Result<ApprovalVO> detail(@ApiParam("审批ID") @PathVariable Long id) {
        return Result.ok(approvalService.detail(id));
    }

    @ApiOperation("获取审批统计概览")
    @GetMapping("/summary")
    public Result<ApprovalSummaryVO> summary() {
        return Result.ok(approvalService.summary());
    }
}
