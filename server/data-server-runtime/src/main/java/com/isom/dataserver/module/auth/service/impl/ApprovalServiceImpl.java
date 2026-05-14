package com.isom.dataserver.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.mapper.AppMapper;
import com.isom.dataserver.module.auth.dto.ApprovalActionReq;
import com.isom.dataserver.module.auth.dto.ApprovalCreateReq;
import com.isom.dataserver.module.auth.dto.ApprovalQuery;
import com.isom.dataserver.module.auth.entity.AppApiAuth;
import com.isom.dataserver.module.auth.entity.Approval;
import com.isom.dataserver.module.auth.mapper.AppApiAuthMapper;
import com.isom.dataserver.module.auth.mapper.ApprovalMapper;
import com.isom.dataserver.module.auth.service.ApprovalService;
import com.isom.dataserver.module.auth.vo.ApprovalSummaryVO;
import com.isom.dataserver.module.auth.vo.ApprovalVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalMapper approvalMapper;
    private final AppApiAuthMapper appApiAuthMapper;
    private final AppMapper appMapper;
    private final ApiDefinitionMapper apiDefinitionMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> submit(ApprovalCreateReq req) {
        // Check duplicate
        Long cnt = approvalMapper.selectCount(new LambdaQueryWrapper<Approval>()
                .eq(Approval::getAppId, req.getAppId())
                .eq(Approval::getApiId, req.getApiId())
                .eq(Approval::getStatus, "PENDING"));
        if (cnt > 0) throw new BizException(ErrorCode.DUPLICATE_APPLY);

        Approval approval = new Approval();
        approval.setType(req.getType() != null ? req.getType() : "API_AUTH");
        approval.setAppId(req.getAppId());
        approval.setApiId(req.getApiId());
        approval.setApplicant(1L);
        approval.setStatus("PENDING");
        approval.setReason(req.getReason());
        approval.setPayload("{}");
        approvalMapper.insert(approval);

        // Phase 1: Auto-approval
        approval.setStatus("APPROVED");
        approval.setApprover(2L); // system user
        approval.setComment("系统自动审批");
        approvalMapper.updateById(approval);

        // Create auth record
        AppApiAuth auth = new AppApiAuth();
        auth.setAppId(req.getAppId());
        auth.setApiId(req.getApiId());
        auth.setQpsLimit(req.getQpsLimit() != null ? req.getQpsLimit() : 100);
        auth.setDailyLimit(req.getDailyLimit() != null ? req.getDailyLimit() : 10000);
        auth.setEffectiveFrom(LocalDateTime.now());
        auth.setEffectiveTo(LocalDateTime.now().plusYears(1));
        auth.setStatus("EFFECTIVE");
        auth.setApprovalId(approval.getId());
        appApiAuthMapper.insert(auth);

        Map<String, Object> result = new HashMap<>();
        result.put("approvalId", approval.getId());
        result.put("status", "APPROVED");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> action(Long id, ApprovalActionReq req) {
        Approval approval = approvalMapper.selectById(id);
        if (approval == null) throw new BizException(ErrorCode.APPROVAL_NOT_FOUND);
        if (!"PENDING".equals(approval.getStatus())) {
            throw new BizException(ErrorCode.INVALID_STATUS, "审批已处理");
        }

        if ("APPROVE".equals(req.getAction())) {
            approval.setStatus("APPROVED");
            approval.setApprover(1L);
            approval.setComment(req.getComment());
            approvalMapper.updateById(approval);

            AppApiAuth auth = new AppApiAuth();
            auth.setAppId(approval.getAppId());
            auth.setApiId(approval.getApiId());
            auth.setQpsLimit(100);
            auth.setDailyLimit(10000);
            auth.setEffectiveFrom(LocalDateTime.now());
            auth.setEffectiveTo(LocalDateTime.now().plusYears(1));
            auth.setStatus("EFFECTIVE");
            auth.setApprovalId(approval.getId());
            appApiAuthMapper.insert(auth);
        } else if ("REJECT".equals(req.getAction())) {
            approval.setStatus("REJECTED");
            approval.setApprover(1L);
            approval.setComment(req.getComment());
            approvalMapper.updateById(approval);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", approval.getStatus());
        return result;
    }

    @Override
    public PageResult<ApprovalVO> list(ApprovalQuery query) {
        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Approval::getStatus, query.getStatus());
        }
        if (query.getType() != null && !query.getType().isEmpty()) {
            wrapper.eq(Approval::getType, query.getType());
        }
        if (query.getAppId() != null) {
            wrapper.eq(Approval::getAppId, query.getAppId());
        }
        wrapper.orderByDesc(Approval::getCreatedAt);

        Page<Approval> page = approvalMapper.selectPage(query.toPage(), wrapper);
        List<ApprovalVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public ApprovalVO detail(Long id) {
        Approval approval = approvalMapper.selectById(id);
        if (approval == null) throw new BizException(ErrorCode.APPROVAL_NOT_FOUND);
        return toVO(approval);
    }

    @Override
    public ApprovalSummaryVO summary() {
        List<Map<String, Object>> statusCounts = approvalMapper.countByStatus();
        ApprovalSummaryVO vo = new ApprovalSummaryVO();
        int total = 0;
        for (Map<String, Object> row : statusCounts) {
            String status = (String) row.get("status");
            int cnt = ((Number) row.get("cnt")).intValue();
            total += cnt;
            if ("PENDING".equals(status)) vo.setPending(cnt);
            else if ("APPROVED".equals(status)) vo.setApproved(cnt);
            else if ("REJECTED".equals(status)) vo.setRejected(cnt);
        }
        vo.setTotal(total);
        return vo;
    }

    private ApprovalVO toVO(Approval a) {
        ApprovalVO vo = new ApprovalVO();
        vo.setId(a.getId());
        vo.setType(a.getType());
        vo.setAppId(a.getAppId());
        vo.setApiId(a.getApiId());
        vo.setApplicant(a.getApplicant());
        vo.setApplicantName("管理员");
        vo.setApprover(a.getApprover());
        vo.setApproverName(a.getApprover() != null ? (a.getApprover() == 2L ? "系统" : "管理员") : null);
        vo.setStatus(a.getStatus());
        vo.setReason(a.getReason());
        vo.setComment(a.getComment());

        if (a.getAppId() != null) {
            App app = appMapper.selectById(a.getAppId());
            if (app != null) vo.setAppName(app.getName());
        }
        if (a.getApiId() != null) {
            ApiDefinition api = apiDefinitionMapper.selectById(a.getApiId());
            if (api != null) vo.setApiName(api.getName());
        }

        if (a.getCreatedAt() != null) vo.setCreatedAt(a.getCreatedAt().format(FMT));
        if (a.getUpdatedAt() != null) vo.setUpdatedAt(a.getUpdatedAt().format(FMT));
        return vo;
    }
}
