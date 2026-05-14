package com.isom.dataserver.module.datasource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "数据源详情")
public class DatasourceVO {
    @ApiModelProperty("数据源ID")
    private Long id;
    @ApiModelProperty("数据源名称")
    private String name;
    @ApiModelProperty("数据源类型（mysql/postgresql）")
    private String type;
    @ApiModelProperty("主机地址")
    private String host;
    @ApiModelProperty("端口号")
    private Integer port;
    @ApiModelProperty("数据库名")
    private String databaseName;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("JDBC附加参数")
    private String jdbcParams;
    @ApiModelProperty("状态（enabled/disabled）")
    private String status;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("被引用的API数量")
    private Integer referencedApiCount;
    @ApiModelProperty("创建人ID")
    private Long createdBy;
    @ApiModelProperty("创建人姓名")
    private String createdByName;
    @ApiModelProperty("创建时间")
    private String createdAt;
    @ApiModelProperty("更新时间")
    private String updatedAt;
}
