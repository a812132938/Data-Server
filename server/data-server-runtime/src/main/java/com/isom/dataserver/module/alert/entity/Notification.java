package com.isom.dataserver.module.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiver;
    private Long senderId;
    private String type;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    private String status;
    private LocalDateTime readAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
