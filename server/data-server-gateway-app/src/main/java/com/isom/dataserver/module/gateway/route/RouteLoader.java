package com.isom.dataserver.module.gateway.route;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteLoader {

    private final ApiDefinitionMapper apiDefinitionMapper;
    private final RouteTable routeTable;

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedDelay = 300000)
    public void reload() {
        try {
            List<ApiDefinition> apis = apiDefinitionMapper.selectList(
                    new LambdaQueryWrapper<ApiDefinition>().eq(ApiDefinition::getStatus, "PUBLISHED"));
            List<ApiRouteEntry> entries = new ArrayList<>();
            for (ApiDefinition api : apis) {
                ApiRouteEntry entry = new ApiRouteEntry();
                entry.setApiId(api.getId());
                String path = api.getPath();
                if (!path.startsWith("/")) path = "/" + path;
                entry.setPath(path);
                entry.setMethod(api.getMethod());
                entry.setDatasourceId(api.getDatasourceId());
                entry.setSqlTemplate(api.getSqlTemplate());
                entry.setTimeoutMs(api.getTimeoutMs());
                entry.setCacheEnable(api.getCacheEnable());
                entry.setCacheTtl(api.getCacheTtl());
                entry.setDefaultQpsLimit(api.getDefaultQpsLimit());
                entry.setStatus(api.getStatus());
                entries.add(entry);
            }
            routeTable.replaceAll(entries);
            log.info("Loaded {} routes", entries.size());
        } catch (Exception e) {
            routeTable.markReloadFailed(e.getMessage());
            log.error("Route reload failed", e);
        }
    }
}
