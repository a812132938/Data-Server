package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建/更新API分组请求")
public class ApiGroupReq {
    @ApiModelProperty(value = "分组名称", required = true, example = "用户管理")
    private String name;
    @ApiModelProperty(value = "父级分组ID，顶级传0", example = "0")
    private Long parentId;
    @ApiModelProperty(value = "排序序号", example = "1")
    private Integer sort;
    @ApiModelProperty(value = "描述")
    private String description;
}
