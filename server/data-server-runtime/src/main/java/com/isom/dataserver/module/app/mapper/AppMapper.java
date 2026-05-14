package com.isom.dataserver.module.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.app.entity.App;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppMapper extends BaseMapper<App> {
}
