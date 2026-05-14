package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "解析SQL参数请求")
public class ParseParamsReq {
    @ApiModelProperty(value = "SQL模板", required = true, example = "SELECT * FROM users WHERE id = #{id}")
    private String sqlTemplate;
}
