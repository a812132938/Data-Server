package com.isom.dataserver.module.api.service;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SqlSecurityValidator {

    private static final Pattern DOLLAR_PLACEHOLDER = Pattern.compile("\\$\\{[^}]+}");
    private static final Pattern DANGEROUS_KEYWORDS = Pattern.compile(
            "\\b(DROP|ALTER|TRUNCATE|CREATE|INSERT|UPDATE|DELETE|GRANT|REVOKE|EXEC|EXECUTE)\\b",
            Pattern.CASE_INSENSITIVE);

    public void validate(String sqlTemplate) {
        if (sqlTemplate == null || sqlTemplate.trim().isEmpty()) {
            return;
        }
        if (DOLLAR_PLACEHOLDER.matcher(sqlTemplate).find()) {
            throw new BizException(ErrorCode.SQL_DOLLAR_PLACEHOLDER);
        }
        String cleaned = removeSqlComments(sqlTemplate);
        if (DANGEROUS_KEYWORDS.matcher(cleaned).find()) {
            throw new BizException(ErrorCode.SQL_NOT_SELECT, "SQL 包含危险关键字");
        }
        if (!cleaned.trim().toUpperCase().startsWith("SELECT")) {
            throw new BizException(ErrorCode.SQL_NOT_SELECT);
        }
    }

    private String removeSqlComments(String sql) {
        return sql.replaceAll("--[^\n]*", "")
                  .replaceAll("/\\*[\\s\\S]*?\\*/", "")
                  .trim();
    }
}
