package com.isom.dataserver.module.api.vo;

import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "SQL参数解析结果")
public class ParseParamsResp {
    @ApiModelProperty("解析出的参数列表")
    private List<ApiUpsertReq.ApiParamItem> params;
    @ApiModelProperty("解析警告信息")
    private List<String> warnings;
}
