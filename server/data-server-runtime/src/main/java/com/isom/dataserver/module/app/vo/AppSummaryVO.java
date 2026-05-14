package com.isom.dataserver.module.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "应用统计概览")
public class AppSummaryVO {
    @ApiModelProperty("应用总数")
    private int total;
    @ApiModelProperty("启用中数量")
    private int active;
    @ApiModelProperty("已禁用数量")
    private int disabled;
}
