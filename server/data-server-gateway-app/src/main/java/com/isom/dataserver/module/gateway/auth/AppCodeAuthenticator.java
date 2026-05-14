package com.isom.dataserver.module.gateway.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.mapper.AppMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppCodeAuthenticator {

    private final AppMapper appMapper;

    public Long authenticate(String appCode) {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>()
                .eq(App::getAppCode, appCode).eq(App::getStatus, "ACTIVE"));
        if (app == null) {
            throw new BizException(ErrorCode.AUTH_FAILED, "AppCode 无效");
        }
        return app.getId();
    }
}
