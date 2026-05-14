package com.isom.dataserver.module.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "API统计概览")
public class ApiSummaryVO {
    @ApiModelProperty("API总数")
    private int total;
    @ApiModelProperty("草稿状态数量")
    private int draft;
    @ApiModelProperty("测试中数量")
    private int testing;
    @ApiModelProperty("已发布数量")
    private int published;
    @ApiModelProperty("已下线数量")
    private int offline;
}
