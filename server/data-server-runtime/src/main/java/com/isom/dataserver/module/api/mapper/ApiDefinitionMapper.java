package com.isom.dataserver.module.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.util.List;

@Mapper
public interface ApiDefinitionMapper extends BaseMapper<ApiDefinition> {

    @Select("SELECT status, COUNT(*) as cnt FROM api_definition GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
