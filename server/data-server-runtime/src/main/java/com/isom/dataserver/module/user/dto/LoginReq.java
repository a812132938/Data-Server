package com.isom.dataserver.module.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "用户登录请求")
public class LoginReq {

    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "登录名", required = true, example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true, example = "admin123")
    private String password;
}
