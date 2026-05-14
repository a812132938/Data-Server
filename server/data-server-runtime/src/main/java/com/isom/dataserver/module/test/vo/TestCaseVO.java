package com.isom.dataserver.module.test.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "测试用例详情")
public class TestCaseVO {
    @ApiModelProperty("用例ID")
    private Long id;
    @ApiModelProperty("所属API ID")
    private Long apiId;
    @ApiModelProperty("用例名称")
    private String name;
    @ApiModelProperty("输入参数（JSON字符串）")
    private String inputParams;
    @ApiModelProperty("断言规则（JSON字符串）")
    private String assertions;
    @ApiModelProperty("是否启用")
    private Boolean enabled;
    @ApiModelProperty("最近一次运行状态（SUCCESS/FAIL）")
    private String lastRunStatus;
    @ApiModelProperty("创建时间")
    private String createdAt;
}
