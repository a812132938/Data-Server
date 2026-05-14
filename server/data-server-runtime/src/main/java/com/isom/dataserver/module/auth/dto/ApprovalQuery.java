package com.isom.dataserver.module.auth.dto;

import com.isom.dataserver.common.page.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "审批查询条件")
public class ApprovalQuery extends PageQuery {
    @ApiModelProperty(value = "状态（pending/approved/rejected）")
    private String status;
    @ApiModelProperty(value = "审批类型")
    private String type;
    @ApiModelProperty(value = "应用ID")
    private Long appId;
}
