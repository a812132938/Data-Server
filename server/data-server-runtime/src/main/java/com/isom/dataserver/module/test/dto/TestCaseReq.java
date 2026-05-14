package com.isom.dataserver.module.test.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建/更新测试用例请求")
public class TestCaseReq {
    @ApiModelProperty(value = "用例名称", required = true, example = "查询ID为1的用户")
    private String name;
    @ApiModelProperty(value = "输入参数（对象或JSON字符串）")
    private Object inputParams;
    @ApiModelProperty(value = "断言规则（数组或JSON字符串）")
    private Object assertions;
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;
}
