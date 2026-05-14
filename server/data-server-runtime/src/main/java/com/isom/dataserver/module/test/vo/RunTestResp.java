package com.isom.dataserver.module.test.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(description = "API测试执行结果")
public class RunTestResp {
    @ApiModelProperty("HTTP 状态码")
    private int httpStatus;
    @ApiModelProperty("业务码")
    private int bizCode;
    @ApiModelProperty("耗时（毫秒）")
    private int costMs;
    @ApiModelProperty("响应体")
    private List<Map<String, Object>> data;
    @ApiModelProperty("数据是否已脱敏")
    private boolean masked;
}
