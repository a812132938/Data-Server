package com.isom.dataserver.module.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_response_field")
public class ApiResponseField {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long apiId;
    private String fieldName;
    private String alias;
    private String dataType;
    private String description;
    @TableField("`sensitive`")
    private Integer sensitive;
    private String sensitiveRule;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
