package com.isom.dataserver.module.gateway.auth;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.crypto.AesCryptoUtil;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.mapper.AppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AppKeyAuthenticator {

    private final AppMapper appMapper;
    private final AesCryptoUtil aesCryptoUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public Long authenticate(String appKey, String sign, String timestamp, String nonce, HttpServletRequest request) {
        if (sign == null || timestamp == null || nonce == null) {
            throw new BizException(ErrorCode.SIGN_ERROR, "缺少签名参数");
        }

        // Timestamp validation (5 min window)
        long ts = Long.parseLong(timestamp);
        if (Math.abs(System.currentTimeMillis() - ts) > 300000) {
            throw new BizException(ErrorCode.SIGN_ERROR, "时间戳过期");
        }

        // Nonce uniqueness
        String nonceKey = "nonce:" + nonce;
        Boolean set = stringRedisTemplate.opsForValue().setIfAbsent(nonceKey, "1", Duration.ofMinutes(6));
        if (Boolean.FALSE.equals(set)) {
            throw new BizException(ErrorCode.SIGN_ERROR, "nonce 重复");
        }

        App app = appMapper.selectOne(new LambdaQueryWrapper<App>()
                .eq(App::getAppKey, appKey).eq(App::getStatus, "ACTIVE"));
        if (app == null) {
            throw new BizException(ErrorCode.AUTH_FAILED, "应用不存在或已禁用");
        }

        String secret = aesCryptoUtil.decrypt(app.getAppSecretCipher());

        // Build sign string
        String signStr = appKey + timestamp + nonce;
        String expectedSign = SecureUtil.hmacSha256(secret).digestHex(signStr);

        if (!expectedSign.equals(sign)) {
            throw new BizException(ErrorCode.SIGN_ERROR);
        }

        return app.getId();
    }
}
