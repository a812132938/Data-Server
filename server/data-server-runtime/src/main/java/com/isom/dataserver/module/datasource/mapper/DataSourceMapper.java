package com.isom.dataserver.module.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.datasource.entity.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {

    @Select("SELECT COUNT(DISTINCT ad.id) FROM api_definition ad " +
            "JOIN data_source ds ON ad.datasource_id = ds.id WHERE ds.status = 1")
    int countReferencedApis();

    @Select("SELECT COUNT(*) FROM api_definition WHERE datasource_id = #{datasourceId}")
    int countApisByDatasourceId(Long datasourceId);

    @Select("SELECT COUNT(*) FROM api_definition WHERE datasource_id = #{datasourceId} AND status IN ('PUBLISHED','TESTING')")
    int countActiveApisByDatasourceId(Long datasourceId);
}
