package com.isom.dataserver.module.app.service;

import cn.hutool.core.util.RandomUtil;
import org.springframework.stereotype.Component;

@Component
public class CredentialGenerator {

    public String generateAppKey() {
        return "ak_" + RandomUtil.randomString(16);
    }

    public String generateAppSecret() {
        return RandomUtil.randomString(32);
    }

    public String generateAppCode() {
        return "ac_" + RandomUtil.randomString(32);
    }
}
