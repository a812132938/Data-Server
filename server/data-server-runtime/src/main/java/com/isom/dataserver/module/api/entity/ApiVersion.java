package com.isom.dataserver.module.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_version")
public class ApiVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long apiId;
    private String versionNo;
    private String snapshot;
    private String changeLog;
    private String status;
    private Long publishedBy;
    private LocalDateTime publishedAt;
}
