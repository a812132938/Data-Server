package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "发布API请求")
public class PublishReq {
    @ApiModelProperty(value = "变更日志", example = "修复了分页查询的问题")
    private String changeLog;
}
