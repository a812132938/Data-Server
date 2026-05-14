package com.isom.dataserver.module.api.vo;

import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "多表查询设计渲染结果")
public class QueryDesignRenderResp {
    @ApiModelProperty("生成的SQL模板")
    private String sqlTemplate;
    @ApiModelProperty("参数草稿")
    private List<ApiUpsertReq.ApiParamItem> params;
    @ApiModelProperty("返回字段草稿")
    private List<ApiUpsertReq.ApiResponseFieldItem> responseFields;
    @ApiModelProperty("告警信息")
    private List<String> warnings;
}
