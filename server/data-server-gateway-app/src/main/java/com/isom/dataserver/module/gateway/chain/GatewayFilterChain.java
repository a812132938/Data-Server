package com.isom.dataserver.module.gateway.chain;

import java.util.Comparator;
import java.util.List;

public class GatewayFilterChain {

    private final List<GatewayFilter> filters;
    private int index = 0;

    public GatewayFilterChain(List<GatewayFilter> filters) {
        this.filters = filters;
        this.filters.sort(Comparator.comparingInt(GatewayFilter::getOrder));
    }

    public void doFilter(GatewayContext context) {
        if (index < filters.size()) {
            GatewayFilter filter = filters.get(index++);
            filter.doFilter(context, this);
        }
    }
}
