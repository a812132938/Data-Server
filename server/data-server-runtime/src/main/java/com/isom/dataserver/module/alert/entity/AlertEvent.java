package com.isom.dataserver.module.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("alert_event")
public class AlertEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private LocalDateTime firedAt;
    private LocalDateTime resolvedAt;
    private String status;
    private BigDecimal metricValue;
    private String message;
    private String notifyStatus;
    private String notifyMsg;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
