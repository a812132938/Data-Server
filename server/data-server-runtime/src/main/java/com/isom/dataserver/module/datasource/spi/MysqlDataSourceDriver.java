package com.isom.dataserver.module.datasource.spi;

import com.isom.dataserver.common.spi.DataSourceDriver;
import com.isom.dataserver.module.datasource.vo.SchemaVO;
import com.isom.dataserver.module.datasource.vo.TestResultVO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class MysqlDataSourceDriver implements DataSourceDriver {

    @Override
    public String type() {
        return "MYSQL";
    }

    @Override
    public String buildJdbcUrl(String host, int port, String database, String jdbcParams) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        if (jdbcParams != null && !jdbcParams.isEmpty()) {
            url += "&" + jdbcParams;
        }
        return url;
    }

    @Override
    public TestResultVO test(String jdbcUrl, String username, String password, int timeoutSec) {
        long start = System.currentTimeMillis();
        try {
            DriverManager.setLoginTimeout(timeoutSec);
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(timeoutSec);
                stmt.execute("SELECT 1");
                return new TestResultVO(true, "连接成功", System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            return new TestResultVO(false, "连接失败: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public List<SchemaVO.Schema> getSchemas(Connection conn) {
        List<SchemaVO.Schema> schemas = new ArrayList<>();
        try {
            String catalog = conn.getCatalog();
            SchemaVO.Schema schema = new SchemaVO.Schema();
            schema.setName(catalog);

            Map<String, SchemaVO.Table> tableMap = new LinkedHashMap<>();

            String sql = "SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, catalog);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SchemaVO.Table table = new SchemaVO.Table();
                        table.setName(rs.getString("TABLE_NAME"));
                        table.setComment(rs.getString("TABLE_COMMENT"));
                        table.setColumns(new ArrayList<>());
                        table.setForeignKeys(new ArrayList<>());
                        table.setIndexes(new ArrayList<>());
                        tableMap.put(table.getName(), table);
                    }
                }
            }

            String colSql = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY, IS_NULLABLE, COLUMN_COMMENT " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? ORDER BY TABLE_NAME, ORDINAL_POSITION";
            try (PreparedStatement ps = conn.prepareStatement(colSql)) {
                ps.setString(1, catalog);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SchemaVO.Table table = tableMap.get(rs.getString("TABLE_NAME"));
                        if (table != null) {
                            SchemaVO.Column col = new SchemaVO.Column();
                            col.setName(rs.getString("COLUMN_NAME"));
                            col.setType(rs.getString("COLUMN_TYPE"));
                            col.setPk("PRI".equals(rs.getString("COLUMN_KEY")));
                            col.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                            col.setComment(rs.getString("COLUMN_COMMENT"));
                            table.getColumns().add(col);
                        }
                    }
                }
            }

            String fkSql = "SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_SCHEMA, " +
                    "REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = ? AND REFERENCED_TABLE_NAME IS NOT NULL " +
                    "ORDER BY TABLE_NAME, CONSTRAINT_NAME, ORDINAL_POSITION";
            try (PreparedStatement ps = conn.prepareStatement(fkSql)) {
                ps.setString(1, catalog);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SchemaVO.Table table = tableMap.get(rs.getString("TABLE_NAME"));
                        if (table != null) {
                            SchemaVO.ForeignKey fk = new SchemaVO.ForeignKey();
                            fk.setName(rs.getString("CONSTRAINT_NAME"));
                            fk.setTableName(rs.getString("TABLE_NAME"));
                            fk.setColumnName(rs.getString("COLUMN_NAME"));
                            fk.setReferencedSchema(rs.getString("REFERENCED_TABLE_SCHEMA"));
                            fk.setReferencedTableName(rs.getString("REFERENCED_TABLE_NAME"));
                            fk.setReferencedColumnName(rs.getString("REFERENCED_COLUMN_NAME"));
                            table.getForeignKeys().add(fk);
                        }
                    }
                }
            }

            String indexSql = "SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, NON_UNIQUE, SEQ_IN_INDEX " +
                    "FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = ? " +
                    "ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX";
            try (PreparedStatement ps = conn.prepareStatement(indexSql)) {
                ps.setString(1, catalog);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SchemaVO.Table table = tableMap.get(rs.getString("TABLE_NAME"));
                        if (table != null) {
                            String indexName = rs.getString("INDEX_NAME");
                            SchemaVO.Index index = new SchemaVO.Index();
                            index.setName(indexName);
                            index.setColumnName(rs.getString("COLUMN_NAME"));
                            index.setUnique(rs.getInt("NON_UNIQUE") == 0);
                            index.setPrimary("PRIMARY".equalsIgnoreCase(indexName));
                            index.setSeq(rs.getInt("SEQ_IN_INDEX"));
                            table.getIndexes().add(index);
                        }
                    }
                }
            }

            schema.setTables(new ArrayList<>(tableMap.values()));
            schemas.add(schema);
        } catch (SQLException e) {
            throw new RuntimeException("获取 Schema 失败: " + e.getMessage(), e);
        }
        return schemas;
    }
}
