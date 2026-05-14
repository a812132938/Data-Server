package com.isom.dataserver.module.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "应用详情")
public class AppVO {
    @ApiModelProperty("应用ID")
    private Long id;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("应用Key（用于API认证）")
    private String appKey;
    @ApiModelProperty("应用编码（简化认证）")
    private String appCode;
    @ApiModelProperty("负责人ID")
    private Long owner;
    @ApiModelProperty("负责人姓名")
    private String ownerName;
    @ApiModelProperty("联系人")
    private String contact;
    @ApiModelProperty("用途说明")
    private String purpose;
    @ApiModelProperty("状态（active/disabled）")
    private String status;
    @ApiModelProperty("创建时间")
    private String createdAt;
    @ApiModelProperty("更新时间")
    private String updatedAt;
}
