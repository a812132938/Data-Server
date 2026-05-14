package com.isom.dataserver.module.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("call_log")
public class CallLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private Long apiId;
    private String apiPath;
    private Long appId;
    private String clientIp;
    private String requestParams;
    private Integer responseSize;
    private Integer recordCount;
    private Short httpStatus;
    private Integer bizCode;
    private Integer costMs;
    private String errorMsg;
    private String authType;
    private Boolean cacheHit;
    private LocalDate statDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
