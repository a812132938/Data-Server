package com.isom.dataserver.module.gateway.chain;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.gateway.auth.AppCodeAuthenticator;
import com.isom.dataserver.module.gateway.auth.AppKeyAuthenticator;
import com.isom.dataserver.module.gateway.auth.JwtAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class AuthFilter implements GatewayFilter {

    private final JwtAuthenticator jwtAuthenticator;
    private final AppKeyAuthenticator appKeyAuthenticator;
    private final AppCodeAuthenticator appCodeAuthenticator;
    private final HttpServletRequest request;

    @Override
    public void doFilter(GatewayContext context, GatewayFilterChain chain) {
        String authorization = request.getHeader("Authorization");
        String appKey = request.getHeader("X-App-Key");
        String appCode = request.getParameter("app_code");
        if (appCode == null) appCode = request.getHeader("X-App-Code");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            Long userId = jwtAuthenticator.authenticate(authorization.substring(7));
            context.setAuthMode("JWT");
            context.setUserId(userId);
        } else if (appKey != null) {
            String sign = request.getHeader("X-Sign");
            String timestamp = request.getHeader("X-Timestamp");
            String nonce = request.getHeader("X-Nonce");
            Long appId = appKeyAuthenticator.authenticate(appKey, sign, timestamp, nonce, request);
            context.setAuthMode("APP_KEY");
            context.setAppId(appId);
        } else if (appCode != null) {
            Long appId = appCodeAuthenticator.authenticate(appCode);
            context.setAuthMode("APP_CODE");
            context.setAppId(appId);
        } else {
            throw new BizException(ErrorCode.AUTH_FAILED, "缺少认证信息");
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
