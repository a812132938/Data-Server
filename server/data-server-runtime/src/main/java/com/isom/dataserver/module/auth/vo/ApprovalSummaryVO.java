package com.isom.dataserver.module.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "审批统计概览")
public class ApprovalSummaryVO {
    @ApiModelProperty("审批总数")
    private int total;
    @ApiModelProperty("待审批数量")
    private int pending;
    @ApiModelProperty("已通过数量")
    private int approved;
    @ApiModelProperty("已拒绝数量")
    private int rejected;
}
