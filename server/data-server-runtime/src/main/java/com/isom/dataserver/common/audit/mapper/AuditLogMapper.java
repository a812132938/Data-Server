package com.isom.dataserver.common.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.common.audit.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {
}
