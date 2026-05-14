package com.isom.dataserver.module.auth.service;

import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.auth.dto.ApprovalActionReq;
import com.isom.dataserver.module.auth.dto.ApprovalCreateReq;
import com.isom.dataserver.module.auth.dto.ApprovalQuery;
import com.isom.dataserver.module.auth.vo.ApprovalSummaryVO;
import com.isom.dataserver.module.auth.vo.ApprovalVO;

import java.util.Map;

public interface ApprovalService {
    Map<String, Object> submit(ApprovalCreateReq req);
    Map<String, Object> action(Long id, ApprovalActionReq req);
    PageResult<ApprovalVO> list(ApprovalQuery query);
    ApprovalVO detail(Long id);
    ApprovalSummaryVO summary();
}
