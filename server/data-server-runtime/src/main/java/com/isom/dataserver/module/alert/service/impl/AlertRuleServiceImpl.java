package com.isom.dataserver.module.alert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.alert.entity.AlertRule;
import com.isom.dataserver.module.alert.entity.AlertRuleReceiver;
import com.isom.dataserver.module.alert.mapper.AlertRuleMapper;
import com.isom.dataserver.module.alert.mapper.AlertRuleReceiverMapper;
import com.isom.dataserver.module.alert.service.AlertRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;
    private final AlertRuleReceiverMapper alertRuleReceiverMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        AlertRule entity = new AlertRule();
        entity.setName((String) req.get("name"));
        entity.setTargetType((String) req.get("targetType"));
        entity.setTargetId(req.get("targetId") != null ? Long.parseLong(req.get("targetId").toString()) : null);
        entity.setMetric((String) req.get("metric"));
        entity.setOperator((String) req.get("operator"));
        entity.setThreshold(req.get("threshold") != null ? new BigDecimal(req.get("threshold").toString()) : null);
        entity.setWindowSec(req.get("windowSec") != null ? Integer.parseInt(req.get("windowSec").toString()) : 60);
        entity.setSilenceSec(req.get("silenceSec") != null ? Integer.parseInt(req.get("silenceSec").toString()) : 300);
        entity.setChannels(req.get("channels") != null ? req.get("channels").toString() : "INSITE");
        entity.setEnabled(req.get("enabled") != null ? (Boolean.parseBoolean(req.get("enabled").toString()) ? 1 : 0) : 1);
        alertRuleMapper.insert(entity);

        saveReceivers(entity.getId(), req);

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> req) {
        AlertRule entity = alertRuleMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.RULE_INVALID, "规则不存在");

        if (req.get("name") != null) entity.setName((String) req.get("name"));
        if (req.get("targetType") != null) entity.setTargetType((String) req.get("targetType"));
        if (req.get("targetId") != null) entity.setTargetId(Long.parseLong(req.get("targetId").toString()));
        if (req.get("metric") != null) entity.setMetric((String) req.get("metric"));
        if (req.get("operator") != null) entity.setOperator((String) req.get("operator"));
        if (req.get("threshold") != null) entity.setThreshold(new BigDecimal(req.get("threshold").toString()));
        if (req.get("windowSec") != null) entity.setWindowSec(Integer.parseInt(req.get("windowSec").toString()));
        if (req.get("silenceSec") != null) entity.setSilenceSec(Integer.parseInt(req.get("silenceSec").toString()));
        if (req.get("channels") != null) entity.setChannels(req.get("channels").toString());
        if (req.get("enabled") != null) entity.setEnabled(Boolean.parseBoolean(req.get("enabled").toString()) ? 1 : 0);
        alertRuleMapper.updateById(entity);

        if (req.containsKey("receivers")) {
            alertRuleReceiverMapper.delete(new LambdaQueryWrapper<AlertRuleReceiver>().eq(AlertRuleReceiver::getRuleId, id));
            saveReceivers(id, req);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        alertRuleMapper.deleteById(id);
        alertRuleReceiverMapper.delete(new LambdaQueryWrapper<AlertRuleReceiver>().eq(AlertRuleReceiver::getRuleId, id));
    }

    @Override
    public PageResult<Map<String, Object>> list(Map<String, Object> query) {
        int page = query.get("page") != null ? Integer.parseInt(query.get("page").toString()) : 1;
        int size = query.get("size") != null ? Integer.parseInt(query.get("size").toString()) : 10;

        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (query.get("targetType") != null) {
            wrapper.eq(AlertRule::getTargetType, query.get("targetType").toString());
        }
        if (query.get("metric") != null) {
            wrapper.eq(AlertRule::getMetric, query.get("metric").toString());
        }
        if (query.get("enabled") != null) {
            wrapper.eq(AlertRule::getEnabled, Boolean.parseBoolean(query.get("enabled").toString()) ? 1 : 0);
        }
        wrapper.orderByDesc(AlertRule::getCreatedAt);

        Page<AlertRule> p = alertRuleMapper.selectPage(new Page<>(page, size), wrapper);
        List<Map<String, Object>> records = p.getRecords().stream().map(this::ruleToMap).collect(Collectors.toList());
        return PageResult.of(records, p.getTotal(), (int) p.getCurrent(), (int) p.getSize());
    }

    @Override
    public Map<String, Object> detail(Long id) {
        AlertRule entity = alertRuleMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.RULE_INVALID, "规则不存在");
        Map<String, Object> result = ruleToMap(entity);

        List<AlertRuleReceiver> receivers = alertRuleReceiverMapper.selectList(
                new LambdaQueryWrapper<AlertRuleReceiver>().eq(AlertRuleReceiver::getRuleId, id));
        result.put("receivers", receivers.stream().map(AlertRuleReceiver::getReceiver).collect(Collectors.toList()));

        return result;
    }

    @Override
    public void enable(Long id) {
        AlertRule entity = alertRuleMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.RULE_INVALID, "规则不存在");
        entity.setEnabled(1);
        alertRuleMapper.updateById(entity);
    }

    @Override
    public void disable(Long id) {
        AlertRule entity = alertRuleMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.RULE_INVALID, "规则不存在");
        entity.setEnabled(0);
        alertRuleMapper.updateById(entity);
    }

    @SuppressWarnings("unchecked")
    private void saveReceivers(Long ruleId, Map<String, Object> req) {
        Object receivers = req.get("receivers");
        if (receivers instanceof List) {
            for (Object r : (List<?>) receivers) {
                AlertRuleReceiver receiver = new AlertRuleReceiver();
                receiver.setRuleId(ruleId);
                receiver.setReceiver(Long.parseLong(r.toString()));
                alertRuleReceiverMapper.insert(receiver);
            }
        }
    }

    private Map<String, Object> ruleToMap(AlertRule r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("name", r.getName());
        m.put("targetType", r.getTargetType());
        m.put("targetId", r.getTargetId());
        m.put("metric", r.getMetric());
        m.put("operator", r.getOperator());
        m.put("threshold", r.getThreshold());
        m.put("windowSec", r.getWindowSec());
        m.put("silenceSec", r.getSilenceSec());
        m.put("channels", r.getChannels());
        m.put("enabled", r.getEnabled() != null && r.getEnabled() == 1);
        if (r.getCreatedAt() != null) m.put("createdAt", r.getCreatedAt().format(FMT));
        if (r.getUpdatedAt() != null) m.put("updatedAt", r.getUpdatedAt().format(FMT));
        return m;
    }
}
