package com.isom.dataserver.module.datasource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "数据源库表结构")
public class SchemaVO {
    @ApiModelProperty("Schema列表")
    private List<Schema> schemas;

    @Data
    @ApiModel(description = "Schema信息")
    public static class Schema {
        @ApiModelProperty("Schema名称")
        private String name;
        @ApiModelProperty("表列表")
        private List<Table> tables;
    }

    @Data
    @ApiModel(description = "表信息")
    public static class Table {
        @ApiModelProperty("表名")
        private String name;
        @ApiModelProperty("表注释")
        private String comment;
        @ApiModelProperty("列列表")
        private List<Column> columns;
        @ApiModelProperty("外键列表")
        private List<ForeignKey> foreignKeys;
        @ApiModelProperty("索引列表")
        private List<Index> indexes;
    }

    @Data
    @ApiModel(description = "列信息")
    public static class Column {
        @ApiModelProperty("列名")
        private String name;
        @ApiModelProperty("数据类型")
        private String type;
        @ApiModelProperty("是否主键")
        private Boolean pk;
        @ApiModelProperty("是否可为空")
        private Boolean nullable;
        @ApiModelProperty("列注释")
        private String comment;
    }

    @Data
    @ApiModel(description = "外键信息")
    public static class ForeignKey {
        @ApiModelProperty("约束名")
        private String name;
        @ApiModelProperty("本表名")
        private String tableName;
        @ApiModelProperty("本表列名")
        private String columnName;
        @ApiModelProperty("引用Schema")
        private String referencedSchema;
        @ApiModelProperty("引用表名")
        private String referencedTableName;
        @ApiModelProperty("引用列名")
        private String referencedColumnName;
    }

    @Data
    @ApiModel(description = "索引信息")
    public static class Index {
        @ApiModelProperty("索引名")
        private String name;
        @ApiModelProperty("列名")
        private String columnName;
        @ApiModelProperty("是否唯一")
        private Boolean unique;
        @ApiModelProperty("是否主键")
        private Boolean primary;
        @ApiModelProperty("索引内排序")
        private Integer seq;
    }
}
