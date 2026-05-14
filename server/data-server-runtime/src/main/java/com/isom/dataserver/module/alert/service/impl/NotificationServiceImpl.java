package com.isom.dataserver.module.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.isom.dataserver.module.alert.entity.Notification;
import com.isom.dataserver.module.alert.mapper.NotificationMapper;
import com.isom.dataserver.module.alert.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> insite(String status, String type, Integer limit) {
        if (limit == null || limit <= 0) limit = 20;
        Long currentUserId = 1L; // Phase 1: fixed admin user

        // unreadCount
        Long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getReceiver, currentUserId)
                        .eq(Notification::getStatus, "UNREAD"));

        // records
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiver, currentUserId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Notification::getStatus, status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);
        wrapper.last("LIMIT " + limit);

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        List<Map<String, Object>> records = notifications.stream().map(this::toMap).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unreadCount", unreadCount);
        result.put("records", records);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> markRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && "UNREAD".equals(n.getStatus())) {
            n.setStatus("READ");
            n.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(n);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("status", "READ");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> markAllRead() {
        Long currentUserId = 1L; // Phase 1: fixed admin user
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getReceiver, currentUserId)
                .eq(Notification::getStatus, "UNREAD")
                .set(Notification::getStatus, "READ")
                .set(Notification::getReadAt, LocalDateTime.now());

        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getReceiver, currentUserId)
                        .eq(Notification::getStatus, "UNREAD"));
        notificationMapper.update(null, wrapper);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", count);
        return result;
    }

    private Map<String, Object> toMap(Notification n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("title", n.getTitle());
        m.put("content", n.getContent());
        m.put("type", n.getType());
        m.put("status", n.getStatus());

        String senderName = null;
        if (n.getSenderId() != null) {
            senderName = notificationMapper.selectNicknameBySenderId(n.getSenderId());
        }
        m.put("senderName", senderName);

        if (n.getCreatedAt() != null) m.put("createdAt", n.getCreatedAt().format(FMT));
        m.put("bizType", n.getBizType());
        m.put("bizId", n.getBizId());
        return m;
    }
}
