package com.isom.dataserver.module.auth.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "授权详情")
public class AuthVO {
    @ApiModelProperty("授权ID")
    private Long id;
    @ApiModelProperty("应用ID")
    private Long appId;
    @ApiModelProperty("应用名称")
    private String appName;
    @ApiModelProperty("API ID")
    private Long apiId;
    @ApiModelProperty("API名称")
    private String apiName;
    @ApiModelProperty("API路径")
    private String apiPath;
    @ApiModelProperty("QPS限制")
    private Integer qpsLimit;
    @ApiModelProperty("日调用量限制")
    private Integer dailyLimit;
    @ApiModelProperty("生效开始时间")
    private String effectiveFrom;
    @ApiModelProperty("生效结束时间")
    private String effectiveTo;
    @ApiModelProperty("状态（active/revoked）")
    private String status;
    @ApiModelProperty("创建时间")
    private String createdAt;
}
