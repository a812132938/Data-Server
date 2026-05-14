package com.isom.dataserver.module.api.vo;

import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(description = "SQL预览结果")
public class SqlPreviewResp {
    @ApiModelProperty("渲染后的SQL")
    private String renderedSql;
    @ApiModelProperty("参数映射")
    private List<Map<String, Object>> parameterMappings;
    @ApiModelProperty("返回字段草稿")
    private List<ApiUpsertReq.ApiResponseFieldItem> responseFields;
    @ApiModelProperty("样例数据")
    private List<Map<String, Object>> rows;
    @ApiModelProperty("告警信息")
    private List<String> warnings;
}
