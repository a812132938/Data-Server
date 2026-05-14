package com.isom.dataserver.module.datasource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "数据源统计概览")
public class DatasourceSummaryVO {
    @ApiModelProperty("数据源总数")
    private Integer total;
    @ApiModelProperty("已启用数量")
    private Integer enabled;
    @ApiModelProperty("已禁用数量")
    private Integer disabled;
    @ApiModelProperty("被引用的API总数")
    private Integer referencedApiCount;
}
