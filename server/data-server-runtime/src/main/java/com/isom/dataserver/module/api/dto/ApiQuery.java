package com.isom.dataserver.module.api.dto;

import com.isom.dataserver.common.page.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "API查询条件")
public class ApiQuery extends PageQuery {
    @ApiModelProperty(value = "搜索关键词（名称/路径模糊匹配）")
    private String keyword;
    @ApiModelProperty(value = "状态（draft/testing/published/offline）")
    private String status;
    @ApiModelProperty(value = "分组ID")
    private Long groupId;
}
