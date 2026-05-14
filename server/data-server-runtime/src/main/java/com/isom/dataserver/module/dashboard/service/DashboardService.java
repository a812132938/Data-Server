package com.isom.dataserver.module.dashboard.service;

import com.isom.dataserver.module.dashboard.vo.StatusDistVO;
import com.isom.dataserver.module.dashboard.vo.TopRankVO;
import com.isom.dataserver.module.dashboard.vo.TrendVO;

import java.util.Map;

public interface DashboardService {
    TrendVO trend(String startDate, String endDate);
    StatusDistVO statusDist();
    TopRankVO topApi(int limit);
    TopRankVO topApp(int limit);
    Map<String, Object> overview();
}
