package com.isom.dataserver.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.log.entity.CallLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CallLogMapper extends BaseMapper<CallLog> {
}
