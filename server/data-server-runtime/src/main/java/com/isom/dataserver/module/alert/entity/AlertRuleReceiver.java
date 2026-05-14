package com.isom.dataserver.module.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alert_rule_receiver")
public class AlertRuleReceiver {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ruleId;
    private Long receiver;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
