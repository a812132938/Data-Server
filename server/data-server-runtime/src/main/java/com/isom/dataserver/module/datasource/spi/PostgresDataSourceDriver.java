package com.isom.dataserver.module.datasource.spi;

import com.isom.dataserver.common.spi.DataSourceDriver;
import com.isom.dataserver.module.datasource.vo.SchemaVO;
import com.isom.dataserver.module.datasource.vo.TestResultVO;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

@Component
public class PostgresDataSourceDriver implements DataSourceDriver {

    @Override
    public String type() {
        return "POSTGRES";
    }

    @Override
    public String buildJdbcUrl(String host, int port, String database, String jdbcParams) {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        if (jdbcParams != null && !jdbcParams.isEmpty()) {
            url += "?" + jdbcParams;
        }
        return url;
    }

    @Override
    public TestResultVO test(String jdbcUrl, String username, String password, int timeoutSec) {
        long start = System.currentTimeMillis();
        try {
            DriverManager.setLoginTimeout(timeoutSec);
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                if (conn.isValid(timeoutSec)) {
                    return new TestResultVO(true, "连接成功", System.currentTimeMillis() - start);
                }
                return new TestResultVO(false, "连接验证失败", System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            return new TestResultVO(false, "连接失败: " + e.getMessage(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public List<SchemaVO.Schema> getSchemas(Connection conn) {
        List<SchemaVO.Schema> schemas = new ArrayList<>();
        try {
            SchemaVO.Schema schema = new SchemaVO.Schema();
            schema.setName("public");

            Map<String, SchemaVO.Table> tableMap = new LinkedHashMap<>();

            String sql = "SELECT table_name, obj_description((quote_ident(table_schema)||'.'||quote_ident(table_name))::regclass) AS table_comment " +
                    "FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    SchemaVO.Table table = new SchemaVO.Table();
                    table.setName(rs.getString("table_name"));
                    table.setComment(rs.getString("table_comment"));
                    table.setColumns(new ArrayList<>());
                    table.setForeignKeys(new ArrayList<>());
                    table.setIndexes(new ArrayList<>());
                    tableMap.put(table.getName(), table);
                }
            }

            String colSql = "SELECT c.table_name, c.column_name, c.data_type, c.is_nullable, " +
                    "COALESCE(pgd.description, '') AS column_comment, " +
                    "CASE WHEN pk.column_name IS NOT NULL THEN true ELSE false END AS is_pk " +
                    "FROM information_schema.columns c " +
                    "LEFT JOIN pg_catalog.pg_description pgd ON pgd.objoid = (quote_ident(c.table_schema)||'.'||quote_ident(c.table_name))::regclass AND pgd.objsubid = c.ordinal_position " +
                    "LEFT JOIN (SELECT ku.table_name, ku.column_name FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage ku ON tc.constraint_name = ku.constraint_name " +
                    "WHERE tc.constraint_type = 'PRIMARY KEY' AND tc.table_schema = 'public') pk " +
                    "ON c.table_name = pk.table_name AND c.column_name = pk.column_name " +
                    "WHERE c.table_schema = 'public' ORDER BY c.table_name, c.ordinal_position";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(colSql)) {
                while (rs.next()) {
                    SchemaVO.Table table = tableMap.get(rs.getString("table_name"));
                    if (table != null) {
                        SchemaVO.Column col = new SchemaVO.Column();
                        col.setName(rs.getString("column_name"));
                        col.setType(rs.getString("data_type"));
                        col.setPk(rs.getBoolean("is_pk"));
                        col.setNullable("YES".equals(rs.getString("is_nullable")));
                        col.setComment(rs.getString("column_comment"));
                        table.getColumns().add(col);
                    }
                }
            }

            String fkSql = "SELECT tc.constraint_name, kcu.table_name, kcu.column_name, " +
                    "ccu.table_schema AS referenced_schema, ccu.table_name AS referenced_table_name, " +
                    "ccu.column_name AS referenced_column_name " +
                    "FROM information_schema.table_constraints tc " +
                    "JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name " +
                    "AND tc.table_schema = kcu.table_schema " +
                    "JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name " +
                    "AND ccu.table_schema = tc.table_schema " +
                    "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = 'public' " +
                    "ORDER BY kcu.table_name, tc.constraint_name, kcu.ordinal_position";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(fkSql)) {
                while (rs.next()) {
                    SchemaVO.Table table = tableMap.get(rs.getString("table_name"));
                    if (table != null) {
                        SchemaVO.ForeignKey fk = new SchemaVO.ForeignKey();
                        fk.setName(rs.getString("constraint_name"));
                        fk.setTableName(rs.getString("table_name"));
                        fk.setColumnName(rs.getString("column_name"));
                        fk.setReferencedSchema(rs.getString("referenced_schema"));
                        fk.setReferencedTableName(rs.getString("referenced_table_name"));
                        fk.setReferencedColumnName(rs.getString("referenced_column_name"));
                        table.getForeignKeys().add(fk);
                    }
                }
            }

            String indexSql = "SELECT t.relname AS table_name, i.relname AS index_name, a.attname AS column_name, " +
                    "ix.indisunique, ix.indisprimary, array_position(ix.indkey, a.attnum) AS seq " +
                    "FROM pg_class t " +
                    "JOIN pg_index ix ON t.oid = ix.indrelid " +
                    "JOIN pg_class i ON i.oid = ix.indexrelid " +
                    "JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(ix.indkey) " +
                    "JOIN pg_namespace n ON n.oid = t.relnamespace " +
                    "WHERE n.nspname = 'public' AND t.relkind = 'r' " +
                    "ORDER BY t.relname, i.relname, seq";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(indexSql)) {
                while (rs.next()) {
                    SchemaVO.Table table = tableMap.get(rs.getString("table_name"));
                    if (table != null) {
                        SchemaVO.Index index = new SchemaVO.Index();
                        index.setName(rs.getString("index_name"));
                        index.setColumnName(rs.getString("column_name"));
                        index.setUnique(rs.getBoolean("indisunique"));
                        index.setPrimary(rs.getBoolean("indisprimary"));
                        index.setSeq(rs.getInt("seq"));
                        table.getIndexes().add(index);
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
