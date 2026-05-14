package com.isom.dataserver.module.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("alert_rule")
public class AlertRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String targetType;
    private Long targetId;
    private String metric;
    private String operator;
    private BigDecimal threshold;
    private Integer windowSec;
    private Integer silenceSec;
    private String channels;
    private Integer enabled;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
