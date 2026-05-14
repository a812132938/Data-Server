package com.isom.dataserver.module.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(description = "API版本信息")
public class ApiVersionVO {
    @ApiModelProperty("版本ID")
    private Long id;
    @ApiModelProperty("API ID")
    private Long apiId;
    @ApiModelProperty("版本号")
    private String versionNo;
    @ApiModelProperty("变更日志")
    private String changeLog;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("发布人ID")
    private Long publishedBy;
    @ApiModelProperty("发布人姓名")
    private String publishedByName;
    @ApiModelProperty("发布时间")
    private String publishedAt;
    @ApiModelProperty("参数摘要列表，每项包含 name/dataType/required/operator/defaultValue/location")
    private List<Map<String, Object>> paramSummary = new ArrayList<>();
    @ApiModelProperty("SQL模板")
    private String sqlTemplate;
    @ApiModelProperty("参数数量")
    private Integer paramCount;
    @ApiModelProperty("响应字段数量")
    private Integer responseFieldCount;
    @ApiModelProperty("数据源ID")
    private Long datasourceId;
    @ApiModelProperty("超时时间(ms)")
    private Integer timeoutMs;
    @ApiModelProperty("是否开启缓存")
    private Boolean cacheEnable;
    @ApiModelProperty("缓存TTL(s)")
    private Integer cacheTtl;
    @ApiModelProperty("默认QPS限制")
    private Integer defaultQpsLimit;
    @ApiModelProperty("是否为当前生效版本")
    private Boolean isCurrent;
}
