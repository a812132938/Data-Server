package com.isom.dataserver.module.gateway.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticator {

    public Long authenticate(String token) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                throw new BizException(ErrorCode.TOKEN_EXPIRED);
            }
            return Long.parseLong(loginId.toString());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.AUTH_FAILED, "JWT 认证失败: " + e.getMessage());
        }
    }
}
