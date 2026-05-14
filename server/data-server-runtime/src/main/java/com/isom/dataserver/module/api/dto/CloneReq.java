package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "克隆API请求")
public class CloneReq {
    @ApiModelProperty(value = "新API名称", required = true, example = "查询用户列表-副本")
    private String name;
    @ApiModelProperty(value = "新API路径", required = true, example = "/user/list-copy")
    private String path;
}
