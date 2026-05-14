package com.isom.dataserver.module.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.alert.entity.AlertEvent;
import com.isom.dataserver.module.alert.entity.AlertRule;
import com.isom.dataserver.module.alert.mapper.AlertEventMapper;
import com.isom.dataserver.module.alert.mapper.AlertRuleMapper;
import com.isom.dataserver.module.alert.service.AlertEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertEventServiceImpl implements AlertEventService {

    private final AlertEventMapper alertEventMapper;
    private final AlertRuleMapper alertRuleMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<Map<String, Object>> list(Map<String, Object> query) {
        int page = query.get("page") != null ? Integer.parseInt(query.get("page").toString()) : 1;
        int size = query.get("size") != null ? Integer.parseInt(query.get("size").toString()) : 10;

        LambdaQueryWrapper<AlertEvent> wrapper = new LambdaQueryWrapper<>();
        if (query.get("status") != null) {
            wrapper.eq(AlertEvent::getStatus, query.get("status").toString());
        }
        if (query.get("ruleId") != null) {
            wrapper.eq(AlertEvent::getRuleId, Long.parseLong(query.get("ruleId").toString()));
        }
        wrapper.orderByDesc(AlertEvent::getCreatedAt);

        Page<AlertEvent> p = alertEventMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> records = p.getRecords().stream().map(this::eventToMap).collect(Collectors.toList());
        return PageResult.of(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    @Override
    public Map<String, Object> recent(Integer limit) {
        if (limit == null || limit <= 0) limit = 20;

        LambdaQueryWrapper<AlertEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AlertEvent::getCreatedAt);
        wrapper.last("LIMIT " + limit);

        List<AlertEvent> events = alertEventMapper.selectList(wrapper);
        List<Map<String, Object>> records = events.stream().map(this::eventToMap).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        return result;
    }

    private Map<String, Object> eventToMap(AlertEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("ruleId", e.getRuleId());

        AlertRule rule = alertRuleMapper.selectById(e.getRuleId());
        m.put("ruleName", rule != null ? rule.getName() : null);

        m.put("metricValue", e.getMetricValue());
        m.put("status", e.getStatus());
        if (e.getFiredAt() != null) m.put("firedAt", e.getFiredAt().format(FMT));
        if (e.getResolvedAt() != null) m.put("resolvedAt", e.getResolvedAt().format(FMT));
        m.put("notifyStatus", e.getNotifyStatus());
        return m;
    }
}
