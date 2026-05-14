package com.isom.dataserver.module.datasource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isom.dataserver.common.crypto.AesCryptoUtil;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.common.spi.DataSourceDriver;
import com.isom.dataserver.module.datasource.dto.DatasourceQuery;
import com.isom.dataserver.module.datasource.dto.DatasourceUpsertReq;
import com.isom.dataserver.module.datasource.entity.DataSource;
import com.isom.dataserver.module.datasource.mapper.DataSourceMapper;
import com.isom.dataserver.module.datasource.service.DatasourcePoolManager;
import com.isom.dataserver.module.datasource.service.DatasourceService;
import com.isom.dataserver.module.datasource.vo.DatasourceSummaryVO;
import com.isom.dataserver.module.datasource.vo.DatasourceVO;
import com.isom.dataserver.module.datasource.vo.SchemaVO;
import com.isom.dataserver.module.datasource.vo.TestResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DatasourceServiceImpl implements DatasourceService {

    private final DataSourceMapper dataSourceMapper;
    private final DatasourcePoolManager poolManager;
    private final AesCryptoUtil aesCryptoUtil;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> create(DatasourceUpsertReq req) {
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "创建数据源时密码不能为空");
        }
        Long cnt = dataSourceMapper.selectCount(new LambdaQueryWrapper<DataSource>()
                .eq(DataSource::getName, req.getName()));
        if (cnt > 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源名称已存在");
        }

        DataSource entity = new DataSource();
        entity.setName(req.getName());
        entity.setType(normalizeDatasourceType(req.getType()));
        entity.setHost(req.getHost());
        entity.setPort(req.getPort());
        entity.setDatabaseName(req.getDatabaseName());
        entity.setUsername(req.getUsername());
        entity.setPasswordCipher(aesCryptoUtil.encrypt(req.getPassword()));
        entity.setJdbcParams(req.getJdbcParams());
        entity.setDescription(req.getDescription());
        entity.setStatus(1);
        entity.setCreatedBy(1L);

        dataSourceMapper.insert(entity);
        try {
            poolManager.addPool(entity.getId(), entity);
        } catch (Exception e) {
            // Pool creation failed, but entity is saved.
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        return result;
    }

    @Override
    public Map<String, Object> update(Long id, DatasourceUpsertReq req) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        Long cnt = dataSourceMapper.selectCount(new LambdaQueryWrapper<DataSource>()
                .eq(DataSource::getName, req.getName())
                .ne(DataSource::getId, id));
        if (cnt > 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源名称已存在");
        }

        boolean connChanged = !Objects.equals(entity.getHost(), req.getHost())
                || !Objects.equals(entity.getPort(), req.getPort())
                || !Objects.equals(entity.getDatabaseName(), req.getDatabaseName())
                || !Objects.equals(entity.getUsername(), req.getUsername())
                || (req.getPassword() != null && !req.getPassword().isEmpty());

        entity.setName(req.getName());
        entity.setType(normalizeDatasourceType(req.getType()));
        entity.setHost(req.getHost());
        entity.setPort(req.getPort());
        entity.setDatabaseName(req.getDatabaseName());
        entity.setUsername(req.getUsername());
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            entity.setPasswordCipher(aesCryptoUtil.encrypt(req.getPassword()));
        }
        entity.setJdbcParams(req.getJdbcParams());
        entity.setDescription(req.getDescription());

        dataSourceMapper.updateById(entity);

        if (connChanged && entity.getStatus() == 1) {
            poolManager.removePool(id);
            try {
                poolManager.addPool(id, entity);
            } catch (Exception ignored) {
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    public void delete(Long id) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        int apiCount = dataSourceMapper.countApisByDatasourceId(id);
        if (apiCount > 0) {
            throw new BizException(ErrorCode.DS_REFERENCED, "数据源被 " + apiCount + " 个 API 引用，无法删除");
        }
        dataSourceMapper.deleteById(id);
        poolManager.removePool(id);
    }

    @Override
    public PageResult<DatasourceVO> list(DatasourceQuery query) {
        LambdaQueryWrapper<DataSource> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(DataSource::getName, query.getKeyword())
                    .or().like(DataSource::getDescription, query.getKeyword()));
        }
        if (query.getType() != null && !query.getType().isEmpty()) {
            wrapper.eq(DataSource::getType, normalizeDatasourceType(query.getType()));
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(DataSource::getStatus, "ENABLED".equals(query.getStatus()) ? 1 : 0);
        }
        if (query.getCreatedBy() != null) {
            wrapper.eq(DataSource::getCreatedBy, query.getCreatedBy());
        }
        wrapper.orderByDesc(DataSource::getCreatedAt);

        Page<DataSource> page = dataSourceMapper.selectPage(query.toPage(), wrapper);

        List<DatasourceVO> voList = new ArrayList<>();
        for (DataSource ds : page.getRecords()) {
            voList.add(toVO(ds));
        }
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public DatasourceVO detail(Long id) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        return toVO(entity);
    }

    @Override
    public TestResultVO testNew(DatasourceUpsertReq req) {
        DataSourceDriver driver = poolManager.getDriver(req.getType());
        String jdbcUrl = driver.buildJdbcUrl(req.getHost(), req.getPort(),
                req.getDatabaseName(), req.getJdbcParams());
        return driver.test(jdbcUrl, req.getUsername(), req.getPassword(), 30);
    }

    @Override
    public TestResultVO testExisting(Long id) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        DataSourceDriver driver = poolManager.getDriver(entity.getType());
        String password = aesCryptoUtil.decrypt(entity.getPasswordCipher());
        String jdbcUrl = driver.buildJdbcUrl(entity.getHost(), entity.getPort(),
                entity.getDatabaseName(), entity.getJdbcParams());
        return driver.test(jdbcUrl, entity.getUsername(), password, 30);
    }

    @Override
    public SchemaVO schemas(Long id) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        DataSourceDriver driver = poolManager.getDriver(entity.getType());
        try (Connection conn = poolManager.getConnection(id)) {
            SchemaVO vo = new SchemaVO();
            vo.setSchemas(driver.getSchemas(conn));
            return vo;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.DS_CONNECT_FAIL, "获取 Schema 失败: " + e.getMessage());
        }
    }

    @Override
    public DatasourceSummaryVO summary() {
        List<DataSource> all = dataSourceMapper.selectList(null);
        DatasourceSummaryVO vo = new DatasourceSummaryVO();
        vo.setTotal(all.size());
        vo.setEnabled((int) all.stream().filter(d -> d.getStatus() == 1).count());
        vo.setDisabled((int) all.stream().filter(d -> d.getStatus() == 0).count());
        vo.setReferencedApiCount(dataSourceMapper.countReferencedApis());
        return vo;
    }

    @Override
    public void disable(Long id) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        int activeApis = dataSourceMapper.countActiveApisByDatasourceId(id);
        if (activeApis > 0) {
            throw new BizException(ErrorCode.DS_REFERENCED, "数据源被 " + activeApis + " 个活跃 API 引用，无法禁用");
        }
        entity.setStatus(0);
        dataSourceMapper.updateById(entity);
        poolManager.removePool(id);
    }

    @Override
    public void enable(Long id) {
        DataSource entity = dataSourceMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "数据源不存在");
        }
        entity.setStatus(1);
        dataSourceMapper.updateById(entity);
        try {
            poolManager.addPool(id, entity);
        } catch (Exception ignored) {
        }
    }

    private DatasourceVO toVO(DataSource entity) {
        DatasourceVO vo = new DatasourceVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setType(entity.getType());
        vo.setHost(entity.getHost());
        vo.setPort(entity.getPort());
        vo.setDatabaseName(entity.getDatabaseName());
        vo.setUsername(entity.getUsername());
        vo.setJdbcParams(entity.getJdbcParams());
        vo.setStatus(entity.getStatus() == 1 ? "ENABLED" : "DISABLED");
        vo.setDescription(entity.getDescription());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedByName("管理员");
        vo.setReferencedApiCount(dataSourceMapper.countApisByDatasourceId(entity.getId()));
        if (entity.getCreatedAt() != null) {
            vo.setCreatedAt(entity.getCreatedAt().format(FMT));
        }
        if (entity.getUpdatedAt() != null) {
            vo.setUpdatedAt(entity.getUpdatedAt().format(FMT));
        }
        return vo;
    }

    private String normalizeDatasourceType(String type) {
        if (type == null) {
            return null;
        }
        String normalized = type.trim().toUpperCase();
        if ("POSTGRESQL".equals(normalized) || "PGSQL".equals(normalized) || "PG".equals(normalized)) {
            return "POSTGRES";
        }
        return normalized;
    }
}
