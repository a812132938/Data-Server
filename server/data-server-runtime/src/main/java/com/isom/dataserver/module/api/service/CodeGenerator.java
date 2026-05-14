package com.isom.dataserver.module.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private final StringRedisTemplate stringRedisTemplate;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String nextApiCode() {
        String date = LocalDate.now().format(DATE_FMT);
        String key = "api:code:seq:" + date;
        Long seq = stringRedisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1) {
            stringRedisTemplate.expire(key, java.time.Duration.ofDays(2));
        }
        return String.format("API_%s_%04d", date, seq);
    }
}
