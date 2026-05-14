package com.isom.dataserver.module.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.api.entity.ApiVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApiVersionMapper extends BaseMapper<ApiVersion> {

    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(version_no, 2) AS UNSIGNED)), 0) FROM api_version WHERE api_id = #{apiId}")
    int getMaxVersionNo(@Param("apiId") Long apiId);
}
