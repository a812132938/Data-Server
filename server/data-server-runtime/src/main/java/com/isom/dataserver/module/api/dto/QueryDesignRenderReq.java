package com.isom.dataserver.module.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "多表查询设计渲染请求")
public class QueryDesignRenderReq {
    @ApiModelProperty(value = "数据源ID", required = true)
    private Long datasourceId;
    @ApiModelProperty(value = "主表名", required = true)
    private String mainTable;
    @ApiModelProperty(value = "主表别名", example = "t1")
    private String mainAlias;
    @ApiModelProperty("关联表配置")
    private List<JoinItem> joins;
    @ApiModelProperty("查询字段")
    private List<SelectField> selectFields;
    @ApiModelProperty("过滤条件")
    private List<FilterItem> filters;
    @ApiModelProperty("排序字段")
    private List<SortItem> sorts;
    @ApiModelProperty(value = "是否生成分页片段", example = "true")
    private Boolean pagination;

    @Data
    @ApiModel(description = "关联表配置")
    public static class JoinItem {
        @ApiModelProperty(value = "JOIN类型：INNER/LEFT", example = "LEFT")
        private String type;
        @ApiModelProperty(value = "关联表名", required = true)
        private String table;
        @ApiModelProperty(value = "关联表别名", example = "t2")
        private String alias;
        @ApiModelProperty("关联条件")
        private List<JoinCondition> conditions;
    }

    @Data
    @ApiModel(description = "关联条件")
    public static class JoinCondition {
        @ApiModelProperty(value = "左侧字段，格式：别名.字段", example = "b.bill_id")
        private String leftField;
        @ApiModelProperty(value = "操作符，一期固定 =", example = "=")
        private String operator;
        @ApiModelProperty(value = "右侧字段，格式：别名.字段", example = "d.bill_id")
        private String rightField;
    }

    @Data
    @ApiModel(description = "查询字段")
    public static class SelectField {
        @ApiModelProperty(value = "表别名", example = "b")
        private String tableAlias;
        @ApiModelProperty(value = "字段名", required = true)
        private String field;
        @ApiModelProperty(value = "输出别名", example = "billId")
        private String alias;
        @ApiModelProperty("字段描述")
        private String description;
        @ApiModelProperty("是否敏感字段")
        private Boolean sensitive;
        @ApiModelProperty("脱敏规则")
        private String sensitiveRule;
    }

    @Data
    @ApiModel(description = "过滤条件")
    public static class FilterItem {
        @ApiModelProperty(value = "表别名", example = "b")
        private String tableAlias;
        @ApiModelProperty(value = "字段名", required = true)
        private String field;
        @ApiModelProperty(value = "操作符：EQ/NE/GT/GE/LT/LE/LIKE/IN", example = "EQ")
        private String operator;
        @ApiModelProperty(value = "参数名", example = "billId")
        private String paramName;
        @ApiModelProperty("是否必填")
        private Boolean required;
        @ApiModelProperty("默认值")
        private String defaultValue;
        @ApiModelProperty("示例值")
        private String example;
        @ApiModelProperty("参数描述")
        private String description;
    }

    @Data
    @ApiModel(description = "排序字段")
    public static class SortItem {
        @ApiModelProperty(value = "表别名", example = "b")
        private String tableAlias;
        @ApiModelProperty(value = "字段名", required = true)
        private String field;
        @ApiModelProperty(value = "排序方向：ASC/DESC", example = "DESC")
        private String direction;
    }
}
