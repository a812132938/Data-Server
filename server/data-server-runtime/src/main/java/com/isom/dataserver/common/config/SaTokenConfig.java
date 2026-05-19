package com.isom.dataserver.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/api/v1/**")
                    .notMatch(
                            "/api/v1/user/register",
                            "/api/v1/user/login",
                            "/api/v1/docs/**"
                    )
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
        // /gateway/** uses custom auth logic in the filter chain
    }
}
