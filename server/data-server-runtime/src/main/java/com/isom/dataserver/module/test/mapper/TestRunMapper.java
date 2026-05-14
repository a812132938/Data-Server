package com.isom.dataserver.module.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.test.entity.TestRun;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestRunMapper extends BaseMapper<TestRun> {
}
