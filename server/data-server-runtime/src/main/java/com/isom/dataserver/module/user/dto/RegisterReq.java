package com.isom.dataserver.module.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "用户注册请求")
public class RegisterReq {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度需在3-64之间")
    @ApiModelProperty(value = "登录名", required = true, example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在6-64之间")
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;

    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;

    @ApiModelProperty(value = "邮箱", example = "zhangsan@example.com")
    private String email;

    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
}
