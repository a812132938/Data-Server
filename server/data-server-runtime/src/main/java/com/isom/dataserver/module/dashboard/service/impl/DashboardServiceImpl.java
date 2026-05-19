package com.isom.dataserver.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.mapper.AppMapper;
import com.isom.dataserver.module.dashboard.service.DashboardService;
import com.isom.dataserver.module.dashboard.vo.StatusDistVO;
import com.isom.dataserver.module.dashboard.vo.TopRankVO;
import com.isom.dataserver.module.dashboard.vo.TrendVO;
import com.isom.dataserver.module.log.entity.CallLog;
import com.isom.dataserver.module.log.mapper.CallLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CallLogMapper callLogMapper;
    private final ApiDefinitionMapper apiDefinitionMapper;
    private final AppMapper appMapper;

    @Override
    public TrendVO trend(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        List<CallLog> logs = queryLogs(start, end);

        Map<LocalDate, List<CallLog>> grouped = logs.stream()
                .filter(log -> log.getStatDate() != null)
                .collect(Collectors.groupingBy(CallLog::getStatDate));

        TrendVO vo = new TrendVO();
        List<String> dates = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> successes = new ArrayList<>();
        List<Long> fails = new ArrayList<>();
        List<Long> avgCosts = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<CallLog> dayLogs = grouped.getOrDefault(date, Collections.emptyList());
            dates.add(date.toString());
            long total = dayLogs.size();
            long success = dayLogs.stream().filter(this::isSuccess).count();
            long fail = total - success;
            long avgCost = dayLogs.isEmpty() ? 0 :
                    dayLogs.stream().mapToInt(log -> log.getCostMs() != null ? log.getCostMs() : 0).sum() / total;
            totals.add(total);
            successes.add(success);
            fails.add(fail);
            avgCosts.add(avgCost);
        }

        vo.setDates(dates);
        vo.setTotalCounts(totals);
        vo.setSuccessCounts(successes);
        vo.setFailCounts(fails);
        vo.setAvgDurations(avgCosts);
        return vo;
    }

    @Override
    public StatusDistVO statusDist(String timeRange) {
        List<CallLog> logs = queryLogs(rangeStart(timeRange), LocalDate.now());
        long total = logs.size();
        List<Map<String, Object>> statusCounts = logs.stream()
                .collect(Collectors.groupingBy(log -> statusGroup(log.getHttpStatus()), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("status", entry.getKey());
                    item.put("statusGroup", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("ratio", total > 0 ? (double) entry.getValue() / total : 0);
                    return item;
                })
                .collect(Collectors.toList());
        StatusDistVO vo = new StatusDistVO();
        vo.setItems(statusCounts);
        return vo;
    }

    @Override
    public TopRankVO topApi(String timeRange, int limit) {
        List<CallLog> logs = queryLogs(rangeStart(timeRange), LocalDate.now());
        Map<Long, Long> apiCounts = logs.stream()
                .filter(log -> log.getApiId() != null)
                .collect(Collectors.groupingBy(CallLog::getApiId, Collectors.counting()));
        Map<Long, String> apiNames = loadApiNames(apiCounts.keySet());

        List<Map<String, Object>> items = apiCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("apiId", e.getKey());
                    m.put("count", e.getValue());
                    m.put("total", e.getValue());
                    m.put("apiName", apiNames.getOrDefault(e.getKey(), "API #" + e.getKey()));
                    return m;
                })
                .collect(Collectors.toList());

        TopRankVO vo = new TopRankVO();
        vo.setItems(items);
        return vo;
    }

    @Override
    public TopRankVO topApp(String timeRange, int limit) {
        List<CallLog> logs = queryLogs(rangeStart(timeRange), LocalDate.now());
        Map<Long, Long> appCounts = logs.stream()
                .filter(log -> log.getAppId() != null)
                .collect(Collectors.groupingBy(CallLog::getAppId, Collectors.counting()));
        Map<Long, String> appNames = loadAppNames(appCounts.keySet());

        List<Map<String, Object>> items = appCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("appId", e.getKey());
                    m.put("count", e.getValue());
                    m.put("total", e.getValue());
                    m.put("appName", appNames.getOrDefault(e.getKey(), "应用 #" + e.getKey()));
                    return m;
                })
                .collect(Collectors.toList());

        TopRankVO vo = new TopRankVO();
        vo.setItems(items);
        return vo;
    }

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        Long apiCount = apiDefinitionMapper.selectCount(null);
        Long appCount = appMapper.selectCount(null);
        result.put("totalApis", apiCount != null ? apiCount : 0);
        result.put("totalApps", appCount != null ? appCount : 0);

        // 实时从 call_log 统计今日数据
        LambdaQueryWrapper<CallLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(CallLog::getStatDate, LocalDate.now());
        List<CallLog> todayLogs = callLogMapper.selectList(todayWrapper);

        long todayCalls = todayLogs.size();
        long todaySuccess = todayLogs.stream().filter(l -> l.getHttpStatus() != null && l.getHttpStatus() >= 200 && l.getHttpStatus() < 300).count();
        long todayFail = todayCalls - todaySuccess;
        long avgCost = todayLogs.isEmpty() ? 0 : todayLogs.stream().mapToInt(l -> l.getCostMs() != null ? l.getCostMs() : 0).sum() / todayCalls;
        double successRate = todayCalls > 0 ? (double) todaySuccess / todayCalls * 100 : 0;

        result.put("todayCalls", todayCalls);
        result.put("todaySuccess", todaySuccess);
        result.put("todayFail", todayFail);
        result.put("successRate", Math.round(successRate * 100) / 100.0);
        result.put("avgCost", avgCost);
        result.put("errorCount", todayFail);

        return result;
    }

    private List<CallLog> queryLogs(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(CallLog::getStatDate, startDate);
        wrapper.le(CallLog::getStatDate, endDate);
        return callLogMapper.selectList(wrapper);
    }

    private LocalDate rangeStart(String timeRange) {
        int days = "30d".equalsIgnoreCase(timeRange) ? 30 : 7;
        return LocalDate.now().minusDays(days - 1);
    }

    private boolean isSuccess(CallLog log) {
        Short status = log.getHttpStatus();
        return status != null && status >= 200 && status < 300;
    }

    private String statusGroup(Short status) {
        if (status == null || status <= 0) {
            return "其他";
        }
        return (status / 100) + "xx";
    }

    private Map<Long, String> loadApiNames(Set<Long> apiIds) {
        if (apiIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return apiDefinitionMapper.selectBatchIds(apiIds).stream()
                .collect(Collectors.toMap(ApiDefinition::getId, ApiDefinition::getName));
    }

    private Map<Long, String> loadAppNames(Set<Long> appIds) {
        if (appIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appMapper.selectBatchIds(appIds).stream()
                .collect(Collectors.toMap(App::getId, App::getName));
    }
}
