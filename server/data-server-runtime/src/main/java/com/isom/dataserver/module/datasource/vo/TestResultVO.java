package com.isom.dataserver.module.datasource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "数据源连接测试结果")
public class TestResultVO {
    @ApiModelProperty("是否连接成功")
    private Boolean success;
    @ApiModelProperty("结果消息")
    private String message;
    @ApiModelProperty("耗时（毫秒）")
    private Long costMs;
}
