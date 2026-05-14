package com.isom.dataserver.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("app_api_auth")
public class AppApiAuth {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appId;
    private Long apiId;
    private Integer qpsLimit;
    private Integer dailyLimit;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String status;
    private Long approvalId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
