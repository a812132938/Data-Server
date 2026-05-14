package com.isom.dataserver.module.datasource.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel(description = "创建/更新数据源请求")
public class DatasourceUpsertReq {
    @ApiModelProperty(value = "数据源名称", required = true, example = "业务主库")
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @ApiModelProperty(value = "数据源类型（mysql/postgresql）", required = true, example = "mysql")
    @NotBlank(message = "数据源类型不能为空")
    private String type;

    @ApiModelProperty(value = "主机地址", required = true, example = "192.168.1.100")
    @NotBlank(message = "主机不能为空")
    private String host;

    @ApiModelProperty(value = "端口号", required = true, example = "3306")
    @NotNull(message = "端口不能为空")
    @Min(1) @Max(65535)
    private Integer port;

    @ApiModelProperty(value = "数据库名", required = true, example = "my_database")
    @NotBlank(message = "数据库名不能为空")
    private String databaseName;

    @ApiModelProperty(value = "用户名", required = true, example = "root")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "JDBC附加参数", example = "useSSL=false&serverTimezone=Asia/Shanghai")
    private String jdbcParams;

    @ApiModelProperty(value = "描述")
    @Size(max = 255)
    private String description;
}
