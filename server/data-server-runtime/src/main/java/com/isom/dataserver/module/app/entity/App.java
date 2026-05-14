package com.isom.dataserver.module.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("app")
public class App {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String appKey;
    private String appSecretCipher;
    private String appCode;
    private Long owner;
    private String contact;
    private String purpose;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
