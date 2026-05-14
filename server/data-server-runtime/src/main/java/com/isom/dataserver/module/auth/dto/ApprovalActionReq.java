package com.isom.dataserver.module.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "审批操作请求")
public class ApprovalActionReq {
    @ApiModelProperty(value = "操作类型（approve/reject）", required = true, example = "approve")
    private String action;
    @ApiModelProperty(value = "审批意见", example = "同意")
    private String comment;
}
