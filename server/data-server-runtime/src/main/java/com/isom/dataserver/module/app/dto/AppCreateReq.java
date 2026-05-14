package com.isom.dataserver.module.app.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建应用请求")
public class AppCreateReq {
    @ApiModelProperty(value = "应用名称", required = true, example = "数据分析平台")
    private String name;
    @ApiModelProperty(value = "联系人", example = "张三")
    private String contact;
    @ApiModelProperty(value = "用途说明", example = "用于数据分析报表展示")
    private String purpose;
}
