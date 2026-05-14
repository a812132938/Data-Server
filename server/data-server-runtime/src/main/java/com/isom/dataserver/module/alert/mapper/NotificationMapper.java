package com.isom.dataserver.module.alert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isom.dataserver.module.alert.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT nickname FROM `user` WHERE id = #{senderId}")
    String selectNicknameBySenderId(Long senderId);
}
