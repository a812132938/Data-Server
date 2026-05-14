package com.isom.dataserver.module.gateway.chain;

public interface GatewayFilter {
    void doFilter(GatewayContext context, GatewayFilterChain chain);
    int getOrder();
}
