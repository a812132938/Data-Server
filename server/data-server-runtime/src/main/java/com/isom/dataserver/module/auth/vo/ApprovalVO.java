package com.isom.dataserver.module.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "审批详情")
public class ApprovalVO {
    @ApiModelProperty("审批ID")
    private Long id;
    @ApiModelProperty("审批类型")
    private String type;
    @ApiModelProperty("应用ID")
    private Long appId;
    @ApiModelProperty("应用名称")
    private String appName;
    @ApiModelProperty("API ID")
    private Long apiId;
    @ApiModelProperty("API名称")
    private String apiName;
    @ApiModelProperty("申请人ID")
    private Long applicant;
    @ApiModelProperty("申请人姓名")
    private String applicantName;
    @ApiModelProperty("审批人ID")
    private Long approver;
    @ApiModelProperty("审批人姓名")
    private String approverName;
    @ApiModelProperty("状态（pending/approved/rejected）")
    private String status;
    @ApiModelProperty("申请原因")
    private String reason;
    @ApiModelProperty("审批意见")
    private String comment;
    @ApiModelProperty("创建时间")
    private String createdAt;
    @ApiModelProperty("更新时间")
    private String updatedAt;
}
