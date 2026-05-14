package com.isom.dataserver.module.test.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_case")
public class TestCase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long apiId;
    private String name;
    private String inputParams;
    private String assertions;
    private Integer enabled;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
