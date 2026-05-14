package com.isom.dataserver.module.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "创建应用返回结果（含密钥，仅创建时返回一次）")
public class AppCreateVO {
    @ApiModelProperty("应用ID")
    private Long id;
    @ApiModelProperty("应用Key")
    private String appKey;
    @ApiModelProperty("应用密钥（仅此次可见，请妥善保存）")
    private String appSecret;
    @ApiModelProperty("应用编码")
    private String appCode;
}
