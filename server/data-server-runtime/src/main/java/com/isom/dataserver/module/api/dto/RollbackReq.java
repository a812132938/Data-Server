package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "回滚API版本请求")
public class RollbackReq {
    @ApiModelProperty(value = "要回滚到的版本ID", required = true, example = "1")
    private Long versionId;
}
