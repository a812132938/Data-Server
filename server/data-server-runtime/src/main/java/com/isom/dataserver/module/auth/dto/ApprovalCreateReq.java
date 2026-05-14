package com.isom.dataserver.module.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "提交审批申请请求")
public class ApprovalCreateReq {
    @ApiModelProperty(value = "审批类型（api_auth/api_publish等）", required = true, example = "api_auth")
    private String type;
    @ApiModelProperty(value = "应用ID", required = true, example = "1")
    private Long appId;
    @ApiModelProperty(value = "API ID", required = true, example = "1")
    private Long apiId;
    @ApiModelProperty(value = "申请原因", example = "需要调用该接口进行数据查询")
    private String reason;
    @ApiModelProperty(value = "申请的QPS限制", example = "100")
    private Integer qpsLimit;
    @ApiModelProperty(value = "申请的日调用量限制", example = "10000")
    private Integer dailyLimit;
}
