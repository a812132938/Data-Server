package com.isom.dataserver.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    APP_NAME_DUPLICATE(400, 10001, "应用名重复"),
    USERNAME_DUPLICATE(400, 10002, "用户名已存在"),
    USER_NOT_FOUND(404, 10003, "用户不存在"),
    PASSWORD_ERROR(401, 10004, "密码错误"),
    USER_DISABLED(403, 10005, "用户已禁用"),
    AUTH_FAILED(401, 10101, "认证失败"),
    SIGN_ERROR(401, 10102, "签名错误"),
    TOKEN_EXPIRED(401, 10103, "token 过期"),

    API_OFFLINE(400, 20001, "API 已下线"),
    DUPLICATE_APPLY(400, 20002, "重复申请"),
    QPS_OVER_LIMIT(400, 20003, "QPS 超过 API 上限"),
    UNAUTHORIZED(403, 20101, "未授权"),
    AUTH_EXPIRED(403, 20102, "授权过期"),

    DS_CONNECT_FAIL(400, 30001, "数据源连接失败"),
    DS_REFERENCED(400, 30002, "数据源被引用"),
    SQL_SYNTAX_ERROR(400, 30101, "SQL 语法错误"),
    PARAM_SQL_MISMATCH(400, 30102, "参数与 SQL 不一致"),
    SQL_NOT_SELECT(400, 30103, "SQL 包含非 SELECT 语句"),
    SQL_DOLLAR_PLACEHOLDER(400, 30104, "SQL 包含 ${} 占位符"),
    GATE_NOT_PASS(400, 30201, "测试门控未通过"),
    SNAPSHOT_FAIL(500, 30202, "版本快照失败"),

    EXEC_TIMEOUT(504, 40001, "执行超时"),
    SQL_EXEC_FAIL(500, 40002, "SQL 执行失败"),
    PARAM_VALIDATE_FAIL(400, 40003, "参数校验失败"),
    ROUTE_NOT_FOUND(404, 40404, "路由不存在"),

    RATE_LIMITED(429, 50001, "触发限流"),
    DAILY_QUOTA_EXHAUSTED(429, 50002, "日额度用尽"),

    APPROVAL_NOT_FOUND(404, 60001, "审批不存在"),
    NO_APPROVAL_PERMISSION(403, 60002, "无审批权限"),
    INVALID_STATUS(400, 60003, "状态非法"),

    RULE_INVALID(400, 70001, "规则非法"),
    NOTIFY_FAIL(500, 70002, "通知发送失败"),

    INTERNAL_ERROR(500, 90001, "内部错误"),
    DEPENDENCY_UNAVAILABLE(503, 90002, "依赖不可用");

    private final int httpStatus;
    private final int code;
    private final String message;
}
