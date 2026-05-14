package com.isom.dataserver.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageQuery;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.mapper.AppMapper;
import com.isom.dataserver.module.auth.entity.AppApiAuth;
import com.isom.dataserver.module.auth.mapper.AppApiAuthMapper;
import com.isom.dataserver.module.auth.service.AuthService;
import com.isom.dataserver.module.auth.vo.AuthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppApiAuthMapper appApiAuthMapper;
    private final AppMapper appMapper;
    private final ApiDefinitionMapper apiDefinitionMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<AuthVO> listByApp(Long appId, PageQuery query) {
        Page<AppApiAuth> page = appApiAuthMapper.selectPage(query.toPage(),
                new LambdaQueryWrapper<AppApiAuth>().eq(AppApiAuth::getAppId, appId));
        List<AuthVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public PageResult<AuthVO> listByApi(Long apiId, PageQuery query) {
        Page<AppApiAuth> page = appApiAuthMapper.selectPage(query.toPage(),
                new LambdaQueryWrapper<AppApiAuth>().eq(AppApiAuth::getApiId, apiId));
        List<AuthVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public void revoke(Long authId) {
        AppApiAuth auth = appApiAuthMapper.selectById(authId);
        if (auth == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "授权不存在");
        auth.setStatus("REVOKED");
        appApiAuthMapper.updateById(auth);
    }

    private AuthVO toVO(AppApiAuth a) {
        AuthVO vo = new AuthVO();
        vo.setId(a.getId());
        vo.setAppId(a.getAppId());
        vo.setApiId(a.getApiId());
        vo.setQpsLimit(a.getQpsLimit());
        vo.setDailyLimit(a.getDailyLimit());
        vo.setStatus(a.getStatus());

        if (a.getAppId() != null) {
            App app = appMapper.selectById(a.getAppId());
            if (app != null) vo.setAppName(app.getName());
        }
        if (a.getApiId() != null) {
            ApiDefinition api = apiDefinitionMapper.selectById(a.getApiId());
            if (api != null) {
                vo.setApiName(api.getName());
                vo.setApiPath(api.getPath());
            }
        }
        if (a.getEffectiveFrom() != null) vo.setEffectiveFrom(a.getEffectiveFrom().format(FMT));
        if (a.getEffectiveTo() != null) vo.setEffectiveTo(a.getEffectiveTo().format(FMT));
        if (a.getCreatedAt() != null) vo.setCreatedAt(a.getCreatedAt().format(FMT));
        return vo;
    }
}
