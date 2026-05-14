package com.isom.dataserver.common.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operator;
    private String action;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String beforeSnapshot;
    private String afterSnapshot;
    private String ip;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
