package com.isom.dataserver.module.gateway.chain;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.gateway.ratelimit.RedisRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GatewayFilter {

    private final RedisRateLimiter rateLimiter;

    @Override
    public void doFilter(GatewayContext context, GatewayFilterChain chain) {
        Long apiId = context.getRoute().getApiId();
        int qpsLimit = context.getRoute().getDefaultQpsLimit() != null ? context.getRoute().getDefaultQpsLimit() : 100;

        if (!rateLimiter.isAllowed("api:" + apiId, qpsLimit, 1)) {
            throw new BizException(ErrorCode.RATE_LIMITED);
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
