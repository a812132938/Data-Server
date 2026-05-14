package com.isom.dataserver.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.auth.entity.Approval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ApprovalMapper extends BaseMapper<Approval> {

    @Select("SELECT status, COUNT(*) as cnt FROM approval GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
