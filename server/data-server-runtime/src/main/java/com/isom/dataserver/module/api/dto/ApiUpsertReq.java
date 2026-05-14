package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "创建/更新API请求")
public class ApiUpsertReq {
    @ApiModelProperty(value = "所属分组ID", example = "1")
    private Long groupId;
    @ApiModelProperty(value = "API名称", example = "查询用户列表")
    private String name;
    @ApiModelProperty(value = "API路径", example = "/user/list")
    private String path;
    @ApiModelProperty(value = "请求方法（GET/POST）", example = "GET")
    private String method;
    @ApiModelProperty(value = "创建方式（sql/wizard）", example = "sql")
    private String createMode;
    @ApiModelProperty(value = "数据源ID", example = "1")
    private Long datasourceId;
    @ApiModelProperty(value = "SQL模板", example = "SELECT * FROM users WHERE id = #{id}")
    private String sqlTemplate;
    @ApiModelProperty(value = "结构化查询设计（JOIN_WIZARD模式使用）")
    private QueryDesignRenderReq queryDesign;
    @ApiModelProperty(value = "SQL类型（SELECT/INSERT/UPDATE/DELETE）", example = "SELECT")
    private String sqlType;
    @ApiModelProperty(value = "超时时间（毫秒）", example = "5000")
    private Integer timeoutMs;
    @ApiModelProperty(value = "是否启用缓存")
    private Boolean cacheEnable;
    @ApiModelProperty(value = "缓存有效期（秒）", example = "300")
    private Integer cacheTtl;
    @ApiModelProperty(value = "默认QPS限制", example = "100")
    private Integer defaultQpsLimit;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "请求参数列表")
    private List<ApiParamItem> params;
    @ApiModelProperty(value = "响应字段列表")
    private List<ApiResponseFieldItem> responseFields;

    @Data
    @ApiModel(description = "API请求参数项")
    public static class ApiParamItem {
        @ApiModelProperty(value = "参数名", example = "id")
        private String name;
        @ApiModelProperty(value = "参数位置（query/path/body）", example = "query")
        private String location;
        @ApiModelProperty(value = "数据类型（string/int/long等）", example = "long")
        private String dataType;
        @ApiModelProperty(value = "是否必填")
        private Boolean required;
        @ApiModelProperty(value = "默认值")
        private String defaultValue;
        @ApiModelProperty(value = "SQL操作符（=/LIKE/IN等）", example = "=")
        private String operator;
        @ApiModelProperty(value = "校验规则")
        private String validateRule;
        @ApiModelProperty(value = "最小值")
        private String validateMin;
        @ApiModelProperty(value = "最大值")
        private String validateMax;
        @ApiModelProperty(value = "格式说明")
        private String formatDesc;
        @ApiModelProperty(value = "参数描述")
        private String description;
        @ApiModelProperty(value = "示例值", example = "1")
        private String example;
        @ApiModelProperty(value = "排序序号")
        private Integer sort;
    }

    @Data
    @ApiModel(description = "API响应字段项")
    public static class ApiResponseFieldItem {
        @ApiModelProperty(value = "字段名", example = "user_name")
        private String fieldName;
        @ApiModelProperty(value = "别名", example = "userName")
        private String alias;
        @ApiModelProperty(value = "数据类型", example = "string")
        private String dataType;
        @ApiModelProperty(value = "字段描述")
        private String description;
        @ApiModelProperty(value = "是否敏感字段")
        private Boolean sensitive;
        @ApiModelProperty(value = "脱敏规则（phone/email/idcard等）")
        private String sensitiveRule;
        @ApiModelProperty(value = "排序序号")
        private Integer sort;
    }
}
