package com.isom.dataserver.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval")
public class Approval {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private Long appId;
    private Long apiId;
    private Long applicant;
    private Long approver;
    private String status;
    private String reason;
    private String comment;
    private String payload;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
