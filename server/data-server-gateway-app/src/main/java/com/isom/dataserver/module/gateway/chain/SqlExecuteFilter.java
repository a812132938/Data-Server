package com.isom.dataserver.module.gateway.chain;

import com.isom.dataserver.module.datasource.service.DatasourcePoolManager;
import com.isom.dataserver.module.gateway.engine.BoundSqlResult;
import com.isom.dataserver.module.gateway.engine.SqlExecuteEngine;
import com.isom.dataserver.module.gateway.engine.SqlRenderEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SqlExecuteFilter implements GatewayFilter {

    private final SqlRenderEngine sqlRenderEngine;
    private final SqlExecuteEngine sqlExecuteEngine;
    private final DatasourcePoolManager poolManager;

    @Override
    public void doFilter(GatewayContext context, GatewayFilterChain chain) {
        BoundSqlResult boundSql = sqlRenderEngine.render(
                context.getRoute().getSqlTemplate(), context.getRequestParams());
        context.setRenderedSql(boundSql.getRenderedSql());

        int timeout = context.getRoute().getTimeoutMs() != null ? context.getRoute().getTimeoutMs() / 1000 : 30;
        try (Connection conn = poolManager.getConnection(context.getRoute().getDatasourceId())) {
            List<Map<String, Object>> rows = sqlExecuteEngine.execute(conn, boundSql, timeout, 1000);
            context.setResponseData(rows);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
