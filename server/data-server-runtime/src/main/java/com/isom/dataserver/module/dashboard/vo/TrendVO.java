package com.isom.dataserver.module.dashboard.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(description = "调用趋势数据")
public class TrendVO {
    @ApiModelProperty("日期列表")
    private List<String> dates = new ArrayList<>();
    @ApiModelProperty("每日总调用量")
    private List<Long> totalCounts = new ArrayList<>();
    @ApiModelProperty("每日成功调用量")
    private List<Long> successCounts = new ArrayList<>();
    @ApiModelProperty("每日失败调用量")
    private List<Long> failCounts = new ArrayList<>();
    @ApiModelProperty("每日平均耗时（毫秒）")
    private List<Long> avgDurations = new ArrayList<>();
}
