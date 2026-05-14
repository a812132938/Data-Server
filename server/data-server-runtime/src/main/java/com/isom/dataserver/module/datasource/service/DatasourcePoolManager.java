package com.isom.dataserver.module.datasource.service;

import com.isom.dataserver.common.crypto.AesCryptoUtil;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.spi.DataSourceDriver;
import com.isom.dataserver.module.datasource.entity.DataSource;
import com.isom.dataserver.module.datasource.mapper.DataSourceMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatasourcePoolManager {

    private final ConcurrentHashMap<Long, HikariDataSource> pools = new ConcurrentHashMap<>();
    private final DataSourceMapper dataSourceMapper;
    private final AesCryptoUtil aesCryptoUtil;
    private final List<DataSourceDriver> drivers;

    @PostConstruct
    public void init() {
        List<DataSource> list = dataSourceMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DataSource>()
                        .eq(DataSource::getStatus, 1));
        for (DataSource ds : list) {
            try {
                addPool(ds.getId(), ds);
            } catch (Exception e) {
                log.error("Failed to init pool for datasource {}: {}", ds.getId(), e.getMessage());
            }
        }
        log.info("Initialized {} datasource pools", pools.size());
    }

    @PreDestroy
    public void destroy() {
        pools.forEach((id, ds) -> {
            try {
                ds.close();
            } catch (Exception e) {
                log.error("Failed to close pool {}", id, e);
            }
        });
        pools.clear();
    }

    public void addPool(Long id, DataSource entity) {
        DataSourceDriver driver = getDriver(entity.getType());
        String jdbcUrl = driver.buildJdbcUrl(entity.getHost(), entity.getPort(),
                entity.getDatabaseName(), entity.getJdbcParams());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(entity.getUsername());
        config.setPassword(aesCryptoUtil.decrypt(entity.getPasswordCipher()));
        config.setReadOnly(true);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setPoolName("biz-ds-" + id);

        HikariDataSource old = pools.put(id, new HikariDataSource(config));
        if (old != null) {
            try {
                old.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void removePool(Long id) {
        HikariDataSource ds = pools.remove(id);
        if (ds != null) {
            try {
                ds.close();
            } catch (Exception ignored) {
            }
        }
    }

    public HikariDataSource getPool(Long id) {
        return pools.get(id);
    }

    public Connection getConnection(Long id) throws SQLException {
        HikariDataSource ds = getPool(id);
        if (ds == null) {
            throw new BizException(ErrorCode.DS_CONNECT_FAIL, "数据源连接池不存在，id=" + id);
        }
        return ds.getConnection();
    }

    public DataSourceDriver getDriver(String type) {
        String normalizedType = normalizeType(type);
        return drivers.stream()
                .filter(d -> d.type().equalsIgnoreCase(normalizedType))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.DS_CONNECT_FAIL, "不支持的数据源类型: " + type));
    }

    private String normalizeType(String type) {
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
