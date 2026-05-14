package com.isom.dataserver.module.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.crypto.AesCryptoUtil;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.app.dto.AppCreateReq;
import com.isom.dataserver.module.app.dto.AppQuery;
import com.isom.dataserver.module.app.dto.AppUpdateReq;
import com.isom.dataserver.module.app.entity.App;
import com.isom.dataserver.module.app.entity.AppSecretHistory;
import com.isom.dataserver.module.app.mapper.AppMapper;
import com.isom.dataserver.module.app.mapper.AppSecretHistoryMapper;
import com.isom.dataserver.module.app.service.AppService;
import com.isom.dataserver.module.app.service.CredentialGenerator;
import com.isom.dataserver.module.app.vo.AppCreateVO;
import com.isom.dataserver.module.app.vo.AppSummaryVO;
import com.isom.dataserver.module.app.vo.AppVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {

    private final AppMapper appMapper;
    private final AppSecretHistoryMapper appSecretHistoryMapper;
    private final CredentialGenerator credentialGenerator;
    private final AesCryptoUtil aesCryptoUtil;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public AppCreateVO create(AppCreateReq req) {
        Long cnt = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getName, req.getName()));
        if (cnt > 0) throw new BizException(ErrorCode.APP_NAME_DUPLICATE);

        String appKey = credentialGenerator.generateAppKey();
        String appSecret = credentialGenerator.generateAppSecret();
        String appCode = credentialGenerator.generateAppCode();

        App entity = new App();
        entity.setName(req.getName());
        entity.setAppKey(appKey);
        entity.setAppSecretCipher(aesCryptoUtil.encrypt(appSecret));
        entity.setAppCode(appCode);
        entity.setOwner(1L);
        entity.setContact(req.getContact());
        entity.setPurpose(req.getPurpose());
        entity.setStatus("ACTIVE");
        appMapper.insert(entity);

        AppSecretHistory history = new AppSecretHistory();
        history.setAppId(entity.getId());
        history.setSecretCipher(entity.getAppSecretCipher());
        history.setValidFrom(LocalDateTime.now());
        history.setStatus("CURRENT");
        appSecretHistoryMapper.insert(history);

        AppCreateVO vo = new AppCreateVO();
        vo.setId(entity.getId());
        vo.setAppKey(appKey);
        vo.setAppSecret(appSecret);
        vo.setAppCode(appCode);
        return vo;
    }

    @Override
    public Map<String, Object> update(Long id, AppUpdateReq req) {
        App entity = appMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "应用不存在");

        if (req.getName() != null && !req.getName().equals(entity.getName())) {
            Long cnt = appMapper.selectCount(new LambdaQueryWrapper<App>()
                    .eq(App::getName, req.getName()).ne(App::getId, id));
            if (cnt > 0) throw new BizException(ErrorCode.APP_NAME_DUPLICATE);
            entity.setName(req.getName());
        }
        if (req.getContact() != null) entity.setContact(req.getContact());
        if (req.getPurpose() != null) entity.setPurpose(req.getPurpose());
        appMapper.updateById(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    public void delete(Long id) {
        App entity = appMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "应用不存在");
        appMapper.deleteById(id);
    }

    @Override
    public PageResult<AppVO> list(AppQuery query) {
        LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(App::getName, query.getKeyword())
                    .or().like(App::getAppKey, query.getKeyword()));
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(App::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(App::getCreatedAt);

        Page<App> page = appMapper.selectPage(query.toPage(), wrapper);
        List<AppVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public AppVO detail(Long id) {
        App entity = appMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "应用不存在");
        return toVO(entity);
    }

    @Override
    @Transactional
    public Map<String, Object> resetSecret(Long id) {
        App entity = appMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "应用不存在");

        // Grace period for old secret
        appSecretHistoryMapper.selectList(new LambdaQueryWrapper<AppSecretHistory>()
                        .eq(AppSecretHistory::getAppId, id).eq(AppSecretHistory::getStatus, "CURRENT"))
                .forEach(h -> {
                    h.setStatus("GRACE");
                    h.setValidTo(LocalDateTime.now().plusHours(24));
                    appSecretHistoryMapper.updateById(h);
                });

        String newSecret = credentialGenerator.generateAppSecret();
        entity.setAppSecretCipher(aesCryptoUtil.encrypt(newSecret));
        appMapper.updateById(entity);

        AppSecretHistory history = new AppSecretHistory();
        history.setAppId(id);
        history.setSecretCipher(entity.getAppSecretCipher());
        history.setValidFrom(LocalDateTime.now());
        history.setStatus("CURRENT");
        appSecretHistoryMapper.insert(history);

        Map<String, Object> result = new HashMap<>();
        result.put("appKey", entity.getAppKey());
        result.put("appSecret", newSecret);
        return result;
    }

    @Override
    public void disable(Long id) {
        App entity = appMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "应用不存在");
        entity.setStatus("DISABLED");
        appMapper.updateById(entity);
    }

    @Override
    public void enable(Long id) {
        App entity = appMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "应用不存在");
        entity.setStatus("ACTIVE");
        appMapper.updateById(entity);
    }

    @Override
    public AppSummaryVO summary() {
        List<App> all = appMapper.selectList(null);
        AppSummaryVO vo = new AppSummaryVO();
        vo.setTotal(all.size());
        vo.setActive((int) all.stream().filter(a -> "ACTIVE".equals(a.getStatus())).count());
        vo.setDisabled((int) all.stream().filter(a -> "DISABLED".equals(a.getStatus())).count());
        return vo;
    }

    private AppVO toVO(App entity) {
        AppVO vo = new AppVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setAppKey(entity.getAppKey() != null ? entity.getAppKey().substring(0, Math.min(8, entity.getAppKey().length())) + "****" : null);
        vo.setAppCode(entity.getAppCode());
        vo.setOwner(entity.getOwner());
        vo.setOwnerName("管理员");
        vo.setContact(entity.getContact());
        vo.setPurpose(entity.getPurpose());
        vo.setStatus(entity.getStatus());
        if (entity.getCreatedAt() != null) vo.setCreatedAt(entity.getCreatedAt().format(FMT));
        if (entity.getUpdatedAt() != null) vo.setUpdatedAt(entity.getUpdatedAt().format(FMT));
        return vo;
    }
}
