package com.isom.dataserver.module.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_param")
public class ApiParam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long apiId;
    private String name;
    private String location;
    private String dataType;
    private Integer required;
    private String defaultValue;
    private String operator;
    private String validateRule;
    private String validateMin;
    private String validateMax;
    private String formatDesc;
    private String description;
    private String example;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
