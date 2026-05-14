package com.isom.dataserver.module.gateway.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;
    private DefaultRedisScript<Long> rateLimitScript;

    @PostConstruct
    public void init() {
        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rate_limit.lua")));
        rateLimitScript.setResultType(Long.class);
    }

    public boolean isAllowed(String key, int limit, int windowSeconds) {
        try {
            Long result = stringRedisTemplate.execute(rateLimitScript,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    String.valueOf(windowSeconds),
                    String.valueOf(System.currentTimeMillis()));
            return result != null && result == 1;
        } catch (Exception e) {
            log.warn("Rate limit check failed, allowing request: {}", e.getMessage());
            return true;
        }
    }
}
