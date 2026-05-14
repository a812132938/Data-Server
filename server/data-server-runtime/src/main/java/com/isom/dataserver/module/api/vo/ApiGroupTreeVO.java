package com.isom.dataserver.module.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "API分组树节点")
public class ApiGroupTreeVO {
    @ApiModelProperty("分组ID")
    private Long id;
    @ApiModelProperty("分组名称")
    private String name;
    @ApiModelProperty("父级分组ID")
    private Long parentId;
    @ApiModelProperty("排序序号")
    private Integer sort;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("子分组列表")
    private List<ApiGroupTreeVO> children;
}
