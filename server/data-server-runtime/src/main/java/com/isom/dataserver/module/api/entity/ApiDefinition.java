package com.isom.dataserver.module.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_definition")
public class ApiDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private String name;
    private String code;
    private String path;
    private String method;
    private String createMode;
    private Long datasourceId;
    private String sqlTemplate;
    private String sqlType;
    private Integer timeoutMs;
    private Integer cacheEnable;
    private Integer cacheTtl;
    private Integer defaultQpsLimit;
    private String status;
    private Long currentVersionId;
    private String description;
    private Long owner;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
