package com.isom.dataserver.module.test.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_run")
public class TestRun {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long apiId;
    private Long caseId;
    private String runType;
    private String inputParams;
    private String outputBody;
    private Short httpStatus;
    private Integer bizCode;
    private String status;
    private Integer costMs;
    private String errorMsg;
    private Long executedBy;
    private LocalDateTime executedAt;
}
