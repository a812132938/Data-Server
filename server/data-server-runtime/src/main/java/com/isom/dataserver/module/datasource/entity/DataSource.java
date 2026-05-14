package com.isom.dataserver.module.datasource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("data_source")
public class DataSource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String passwordCipher;
    private String jdbcParams;
    private String poolConfig;
    private Integer status;
    private String description;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
