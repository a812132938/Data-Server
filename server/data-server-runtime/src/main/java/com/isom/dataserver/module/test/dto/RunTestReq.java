package com.isom.dataserver.module.test.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel(description = "执行API测试请求")
public class RunTestReq {
    @ApiModelProperty(value = "测试参数（key-value形式）", example = "{\"id\": 1}")
    private Map<String, Object> params;
}
