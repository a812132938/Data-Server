package com.isom.dataserver.module.app.dto;

import com.isom.dataserver.common.page.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "应用查询条件")
public class AppQuery extends PageQuery {
    @ApiModelProperty(value = "搜索关键词（名称模糊匹配）")
    private String keyword;
    @ApiModelProperty(value = "状态（active/disabled）")
    private String status;
}
