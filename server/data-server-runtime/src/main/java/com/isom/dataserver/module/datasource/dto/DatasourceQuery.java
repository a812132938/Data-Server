package com.isom.dataserver.module.datasource.dto;

import com.isom.dataserver.common.page.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "数据源查询条件")
public class DatasourceQuery extends PageQuery {
    @ApiModelProperty(value = "搜索关键词（名称模糊匹配）")
    private String keyword;
    @ApiModelProperty(value = "数据源类型（mysql/postgresql）")
    private String type;
    @ApiModelProperty(value = "状态（enabled/disabled）")
    private String status;
    @ApiModelProperty(value = "创建人ID")
    private Long createdBy;
}
