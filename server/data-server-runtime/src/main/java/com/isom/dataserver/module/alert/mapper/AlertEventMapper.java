package com.isom.dataserver.module.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.alert.entity.AlertEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {
}
