package com.isom.dataserver.module.api.vo;

import com.isom.dataserver.module.api.dto.ApiUpsertReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "API详情")
public class ApiVO {
    @ApiModelProperty("API ID")
    private Long id;
    @ApiModelProperty("所属分组ID")
    private Long groupId;
    @ApiModelProperty("所属分组名称")
    private String groupName;
    @ApiModelProperty("API名称")
    private String name;
    @ApiModelProperty("API编码（唯一标识）")
    private String code;
    @ApiModelProperty("API路径")
    private String path;
    @ApiModelProperty("请求方法")
    private String method;
    @ApiModelProperty("创建方式（sql/wizard）")
    private String createMode;
    @ApiModelProperty("数据源ID")
    private Long datasourceId;
    @ApiModelProperty("数据源名称")
    private String datasourceName;
    @ApiModelProperty("SQL模板")
    private String sqlTemplate;
    @ApiModelProperty("SQL类型")
    private String sqlType;
    @ApiModelProperty("超时时间（毫秒）")
    private Integer timeoutMs;
    @ApiModelProperty("是否启用缓存")
    private Boolean cacheEnable;
    @ApiModelProperty("缓存有效期（秒）")
    private Integer cacheTtl;
    @ApiModelProperty("默认QPS限制")
    private Integer defaultQpsLimit;
    @ApiModelProperty("状态（draft/testing/published/offline）")
    private String status;
    @ApiModelProperty("当前版本ID")
    private Long currentVersionId;
    @ApiModelProperty("当前版本号")
    private String version;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("负责人ID")
    private Long owner;
    @ApiModelProperty("负责人姓名")
    private String ownerName;
    @ApiModelProperty("请求参数列表")
    private List<ApiUpsertReq.ApiParamItem> params;
    @ApiModelProperty("响应字段列表")
    private List<ApiUpsertReq.ApiResponseFieldItem> responseFields;
    @ApiModelProperty("创建时间")
    private String createdAt;
    @ApiModelProperty("更新时间")
    private String updatedAt;
}
