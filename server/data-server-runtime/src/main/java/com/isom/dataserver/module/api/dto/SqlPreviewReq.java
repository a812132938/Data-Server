package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel(description = "SQL预览请求")
public class SqlPreviewReq {
    @ApiModelProperty(value = "数据源ID", required = true)
    private Long datasourceId;
    @ApiModelProperty(value = "SQL模板", required = true)
    private String sqlTemplate;
    @ApiModelProperty("预览参数")
    private Map<String, Object> params;
    @ApiModelProperty(value = "超时时间（毫秒）", example = "5000")
    private Integer timeoutMs;
    @ApiModelProperty(value = "最大返回行数", example = "20")
    private Integer maxRows;
}
