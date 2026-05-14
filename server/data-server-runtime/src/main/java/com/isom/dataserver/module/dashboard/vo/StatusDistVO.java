package com.isom.dataserver.module.dashboard.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(description = "状态分布数据")
public class StatusDistVO {
    @ApiModelProperty("状态分布列表（包含status和count字段）")
    private List<Map<String, Object>> items = new ArrayList<>();
}
