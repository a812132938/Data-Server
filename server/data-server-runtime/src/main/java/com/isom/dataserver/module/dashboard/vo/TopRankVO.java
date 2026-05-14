package com.isom.dataserver.module.dashboard.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(description = "TOP排行数据")
public class TopRankVO {
    @ApiModelProperty("排行列表（包含name和count字段）")
    private List<Map<String, Object>> items = new ArrayList<>();
}
