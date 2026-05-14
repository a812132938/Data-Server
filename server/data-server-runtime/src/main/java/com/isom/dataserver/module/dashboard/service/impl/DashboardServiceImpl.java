package com.isom.dataserver.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.app.mapper.AppMapper;
import com.isom.dataserver.module.dashboard.service.DashboardService;
import com.isom.dataserver.module.dashboard.vo.StatusDistVO;
import com.isom.dataserver.module.dashboard.vo.TopRankVO;
import com.isom.dataserver.module.dashboard.vo.TrendVO;
import com.isom.dataserver.module.log.entity.CallLog;
import com.isom.dataserver.module.log.entity.CallStatDaily;
import com.isom.dataserver.module.log.mapper.CallLogMapper;
import com.isom.dataserver.module.log.mapper.CallStatDailyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CallStatDailyMapper callStatDailyMapper;
    private final CallLogMapper callLogMapper;
    private final ApiDefinitionMapper apiDefinitionMapper;
    private final AppMapper appMapper;

    @Override
    public TrendVO trend(String startDate, String endDate) {
        LambdaQueryWrapper<CallStatDaily> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) wrapper.ge(CallStatDaily::getStatDate, LocalDate.parse(startDate));
        if (endDate != null) wrapper.le(CallStatDaily::getStatDate, LocalDate.parse(endDate));
        wrapper.orderByAsc(CallStatDaily::getStatDate);

        List<CallStatDaily> stats = callStatDailyMapper.selectList(wrapper);

        Map<LocalDate, List<CallStatDaily>> grouped = stats.stream()
                .collect(Collectors.groupingBy(CallStatDaily::getStatDate));

        TrendVO vo = new TrendVO();
        List<String> dates = new ArrayList<>();
        List<Long> totals = new ArrayList<>();
        List<Long> successes = new ArrayList<>();
        List<Long> fails = new ArrayList<>();
        List<Long> avgCosts = new ArrayList<>();

        grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            dates.add(e.getKey().toString());
            long total = e.getValue().stream().mapToInt(CallStatDaily::getTotal).sum();
            long success = e.getValue().stream().mapToInt(CallStatDaily::getSuccess).sum();
            long fail = e.getValue().stream().mapToInt(CallStatDaily::getFail).sum();
            long avgCost = e.getValue().stream().mapToInt(CallStatDaily::getAvgCost).sum() / Math.max(e.getValue().size(), 1);
            totals.add(total);
            successes.add(success);
            fails.add(fail);
            avgCosts.add(avgCost);
        });

        vo.setDates(dates);
        vo.setTotalCounts(totals);
        vo.setSuccessCounts(successes);
        vo.setFailCounts(fails);
        vo.setAvgDurations(avgCosts);
        return vo;
    }

    @Override
    public StatusDistVO statusDist() {
        List<Map<String, Object>> statusCounts = apiDefinitionMapper.countByStatus();
        StatusDistVO vo = new StatusDistVO();
        vo.setItems(statusCounts);
        return vo;
    }

    @Override
    public TopRankVO topApi(int limit) {
        LambdaQueryWrapper<CallStatDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(CallStatDaily::getStatDate, LocalDate.now().minusDays(7));
        List<CallStatDaily> stats = callStatDailyMapper.selectList(wrapper);

        Map<Long, Integer> apiCounts = stats.stream()
                .collect(Collectors.groupingBy(CallStatDaily::getApiId, Collectors.summingInt(CallStatDaily::getTotal)));

        List<Map<String, Object>> items = apiCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("apiId", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        TopRankVO vo = new TopRankVO();
        vo.setItems(items);
        return vo;
    }

    @Override
    public TopRankVO topApp(int limit) {
        LambdaQueryWrapper<CallStatDaily> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(CallStatDaily::getStatDate, LocalDate.now().minusDays(7));
        wrapper.isNotNull(CallStatDaily::getAppId);
        List<CallStatDaily> stats = callStatDailyMapper.selectList(wrapper);

        Map<Long, Integer> appCounts = stats.stream()
                .filter(s -> s.getAppId() != null)
                .collect(Collectors.groupingBy(CallStatDaily::getAppId, Collectors.summingInt(CallStatDaily::getTotal)));

        List<Map<String, Object>> items = appCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("appId", e.getKey());
                    m.put("count", e.getValue());
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
}
