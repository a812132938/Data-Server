package com.isom.dataserver.module.gateway.chain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.auth.entity.AppApiAuth;
import com.isom.dataserver.module.auth.mapper.AppApiAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuthzFilter implements GatewayFilter {

    private final AppApiAuthMapper appApiAuthMapper;

    @Override
    public void doFilter(GatewayContext context, GatewayFilterChain chain) {
        if ("JWT".equals(context.getAuthMode())) {
            chain.doFilter(context);
            return;
        }

        Long appId = context.getAppId();
        Long apiId = context.getRoute().getApiId();

        AppApiAuth auth = appApiAuthMapper.selectOne(
                new LambdaQueryWrapper<AppApiAuth>()
                        .eq(AppApiAuth::getAppId, appId)
                        .eq(AppApiAuth::getApiId, apiId)
                        .eq(AppApiAuth::getStatus, "EFFECTIVE"));

        if (auth == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        if (auth.getEffectiveTo() != null && auth.getEffectiveTo().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.AUTH_EXPIRED);
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
