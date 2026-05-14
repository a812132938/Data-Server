package com.isom.dataserver.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.crypto.AesCryptoUtil;
import com.isom.dataserver.module.user.entity.User;
import com.isom.dataserver.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时检测并修复占位符密码（v1:CHANGE_ME）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String PLACEHOLDER = "v1:CHANGE_ME";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final UserMapper userMapper;
    private final AesCryptoUtil aesCryptoUtil;

    @Override
    public void run(ApplicationArguments args) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getPasswordCipher, PLACEHOLDER)
        );
        for (User user : users) {
            user.setPasswordCipher(aesCryptoUtil.encrypt(DEFAULT_PASSWORD));
            userMapper.updateById(user);
            log.info("已重置用户 [{}] 的占位符密码为默认密码", user.getUsername());
        }
    }
}
