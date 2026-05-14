package com.isom.dataserver.module.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "登录响应")
public class LoginVO {

    @ApiModelProperty(value = "访问令牌")
    private String token;

    @ApiModelProperty(value = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @ApiModelProperty(value = "过期时间（秒）", example = "86400")
    private Long expiresIn;
}
