package com.isom.dataserver.module.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("call_stat_daily")
public class CallStatDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate statDate;
    private Long apiId;
    private Long appId;
    private Integer total;
    private Integer success;
    private Integer fail;
    private Integer avgCost;
    private Integer p95Cost;
    private Integer p99Cost;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
