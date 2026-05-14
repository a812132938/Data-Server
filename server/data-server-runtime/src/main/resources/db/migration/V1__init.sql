SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Data Server 初始化脚本
--
-- 默认登录账号：
--   用户名：admin
--   密码：admin123
--
-- 说明：
--   user.password_cipher 使用 v1:CHANGE_ME 占位符初始化。
--   Admin 服务启动后，DataInitializer 会将占位符自动替换为 ENCRYPT_KEY 加密后的 admin123。
--   不建议在 SQL 中直接写入明文密码或固定密文，避免不同环境 ENCRYPT_KEY 不一致导致无法登录。

-- T00 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`        VARCHAR(64)  NOT NULL                COMMENT '登录名',
  `password_cipher` VARCHAR(256) NOT NULL                COMMENT '密码密文',
  `nickname`        VARCHAR(64)      NULL DEFAULT NULL   COMMENT '显示名',
  `email`           VARCHAR(128)     NULL DEFAULT NULL   COMMENT '邮箱',
  `phone`           VARCHAR(32)      NULL DEFAULT NULL   COMMENT '手机号',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '1=ENABLED，0=DISABLED',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 默认账号密码：
--   admin / admin123
--   system / admin123
-- 密码字段先写入占位符，启动时由 DataInitializer 按当前 ENCRYPT_KEY 加密更新。
INSERT INTO `user` (`id`, `username`, `password_cipher`, `nickname`, `status`) VALUES
  (1, 'admin',  'v1:CHANGE_ME', '管理员', 1),
  (2, 'system', 'v1:CHANGE_ME', '系统',   1);

-- T01 数据源
DROP TABLE IF EXISTS `data_source`;
CREATE TABLE `data_source` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(64)  NOT NULL,
  `type`            VARCHAR(16)  NOT NULL,
  `host`            VARCHAR(128) NOT NULL,
  `port`            INT          NOT NULL,
  `database_name`   VARCHAR(64)  NOT NULL,
  `username`        VARCHAR(64)  NOT NULL,
  `password_cipher` VARCHAR(512) NOT NULL,
  `jdbc_params`     VARCHAR(512)     NULL DEFAULT NULL,
  `pool_config`     JSON             NULL DEFAULT NULL,
  `status`          TINYINT      NOT NULL DEFAULT 1,
  `description`     VARCHAR(255)     NULL DEFAULT NULL,
  `created_by`      BIGINT       NOT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源';

-- T02 API 分组
DROP TABLE IF EXISTS `api_group`;
CREATE TABLE `api_group` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64)  NOT NULL,
  `parent_id`   BIGINT       NOT NULL DEFAULT 0,
  `sort`        INT          NOT NULL DEFAULT 0,
  `description` VARCHAR(255)     NULL DEFAULT NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_parent` (`name`, `parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 分组';

-- T03 API 定义
DROP TABLE IF EXISTS `api_definition`;
CREATE TABLE `api_definition` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
  `group_id`           BIGINT           NULL DEFAULT NULL,
  `name`               VARCHAR(128) NOT NULL,
  `code`               VARCHAR(64)  NOT NULL,
  `path`               VARCHAR(255) NOT NULL,
  `method`             VARCHAR(8)   NOT NULL,
  `create_mode`        VARCHAR(16)  NOT NULL,
  `datasource_id`      BIGINT       NOT NULL,
  `sql_template`       MEDIUMTEXT   NOT NULL,
  `sql_type`           VARCHAR(16)  NOT NULL DEFAULT 'SELECT',
  `timeout_ms`         INT          NOT NULL DEFAULT 5000,
  `cache_enable`       TINYINT      NOT NULL DEFAULT 0,
  `cache_ttl`          INT              NULL DEFAULT NULL,
  `default_qps_limit`  INT          NOT NULL DEFAULT 100,
  `status`             VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',
  `current_version_id` BIGINT           NULL DEFAULT NULL,
  `description`        TEXT             NULL,
  `owner`              BIGINT       NOT NULL,
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_path_method` (`path`, `method`),
  KEY `idx_status` (`status`),
  KEY `idx_owner` (`owner`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_datasource_id` (`datasource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 定义';

-- T04 API 参数
DROP TABLE IF EXISTS `api_param`;
CREATE TABLE `api_param` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `api_id`        BIGINT       NOT NULL,
  `name`          VARCHAR(64)  NOT NULL,
  `location`      VARCHAR(16)  NOT NULL DEFAULT 'QUERY',
  `data_type`     VARCHAR(16)  NOT NULL,
  `required`      TINYINT      NOT NULL DEFAULT 0,
  `default_value` VARCHAR(255)     NULL DEFAULT NULL,
  `operator`      VARCHAR(16)      NULL DEFAULT NULL,
  `validate_rule` VARCHAR(255)     NULL DEFAULT NULL,
  `validate_min`  VARCHAR(64)      NULL DEFAULT NULL,
  `validate_max`  VARCHAR(64)      NULL DEFAULT NULL,
  `format_desc`   VARCHAR(128)     NULL DEFAULT NULL,
  `description`   VARCHAR(255)     NULL DEFAULT NULL,
  `example`       VARCHAR(255)     NULL DEFAULT NULL,
  `sort`          INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_name` (`api_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 参数';

-- T05 API 返回字段
DROP TABLE IF EXISTS `api_response_field`;
CREATE TABLE `api_response_field` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `api_id`         BIGINT       NOT NULL,
  `field_name`     VARCHAR(64)  NOT NULL,
  `alias`          VARCHAR(64)      NULL DEFAULT NULL,
  `data_type`      VARCHAR(16)  NOT NULL,
  `description`    VARCHAR(255)     NULL DEFAULT NULL,
  `sensitive`       TINYINT      NOT NULL DEFAULT 0,
  `sensitive_rule` VARCHAR(64)      NULL DEFAULT NULL,
  `sort`           INT          NOT NULL DEFAULT 0,
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_field` (`api_id`, `field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 返回字段';

-- T06 测试用例
DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `api_id`       BIGINT       NOT NULL,
  `name`         VARCHAR(128) NOT NULL,
  `input_params` JSON         NOT NULL,
  `assertions`   JSON             NULL DEFAULT NULL,
  `enabled`      TINYINT      NOT NULL DEFAULT 1,
  `created_by`   BIGINT       NOT NULL,
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试用例';

-- T07 测试运行
DROP TABLE IF EXISTS `test_run`;
CREATE TABLE `test_run` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `api_id`       BIGINT       NOT NULL,
  `case_id`      BIGINT           NULL DEFAULT NULL,
  `run_type`     VARCHAR(16)  NOT NULL DEFAULT 'SINGLE',
  `input_params` JSON         NOT NULL,
  `output_body`  JSON             NULL DEFAULT NULL,
  `http_status`  SMALLINT         NULL DEFAULT NULL,
  `biz_code`     INT              NULL DEFAULT NULL,
  `status`       VARCHAR(16)  NOT NULL,
  `cost_ms`      INT          NOT NULL DEFAULT 0,
  `error_msg`    VARCHAR(512)     NULL DEFAULT NULL,
  `executed_by`  BIGINT       NOT NULL,
  `executed_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_api_time` (`api_id`, `executed_at`),
  KEY `idx_case_time` (`case_id`, `executed_at`),
  KEY `idx_status_time` (`status`, `executed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试运行';

-- T08 API 版本
DROP TABLE IF EXISTS `api_version`;
CREATE TABLE `api_version` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `api_id`       BIGINT       NOT NULL,
  `version_no`   VARCHAR(16)  NOT NULL,
  `snapshot`     JSON         NOT NULL,
  `change_log`   TEXT             NULL,
  `status`       VARCHAR(16)  NOT NULL,
  `published_by` BIGINT       NOT NULL,
  `published_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_version` (`api_id`, `version_no`),
  KEY `idx_api_status` (`api_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 版本';

-- T09 应用
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `name`              VARCHAR(128) NOT NULL,
  `app_key`           VARCHAR(32)  NOT NULL,
  `app_secret_cipher` VARCHAR(256) NOT NULL,
  `app_code`          VARCHAR(64)  NOT NULL,
  `owner`             BIGINT       NOT NULL,
  `contact`           VARCHAR(128)     NULL DEFAULT NULL,
  `purpose`           VARCHAR(512)     NULL DEFAULT NULL,
  `status`            VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_app_key` (`app_key`),
  UNIQUE KEY `uk_app_code` (`app_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用';

-- T10 应用密钥历史
DROP TABLE IF EXISTS `app_secret_history`;
CREATE TABLE `app_secret_history` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `app_id`        BIGINT       NOT NULL,
  `secret_cipher` VARCHAR(256) NOT NULL,
  `valid_from`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `valid_to`      DATETIME         NULL DEFAULT NULL,
  `status`        VARCHAR(16)  NOT NULL DEFAULT 'CURRENT',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_app_status` (`app_id`, `status`),
  KEY `idx_valid_to` (`valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用密钥历史';

-- T11 审批
DROP TABLE IF EXISTS `approval`;
CREATE TABLE `approval` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `type`       VARCHAR(16)  NOT NULL DEFAULT 'AUTH_APPLY',
  `app_id`     BIGINT       NOT NULL,
  `api_id`     BIGINT       NOT NULL,
  `applicant`  BIGINT       NOT NULL,
  `approver`   BIGINT           NULL DEFAULT NULL,
  `status`     VARCHAR(16)  NOT NULL,
  `reason`     VARCHAR(512)     NULL DEFAULT NULL,
  `comment`    VARCHAR(512)     NULL DEFAULT NULL,
  `payload`    JSON         NOT NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_approver_status` (`approver`, `status`),
  KEY `idx_applicant_time` (`applicant`, `created_at`),
  KEY `idx_status` (`status`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_api_id` (`api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批';

-- T12 应用-API 授权
DROP TABLE IF EXISTS `app_api_auth`;
CREATE TABLE `app_api_auth` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `app_id`         BIGINT       NOT NULL,
  `api_id`         BIGINT       NOT NULL,
  `qps_limit`      INT              NULL DEFAULT NULL,
  `daily_limit`    INT              NULL DEFAULT NULL,
  `effective_from` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `effective_to`   DATETIME         NULL DEFAULT NULL,
  `status`         VARCHAR(16)  NOT NULL DEFAULT 'EFFECTIVE',
  `approval_id`    BIGINT       NOT NULL,
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_app_api` (`app_id`, `api_id`),
  KEY `idx_status` (`status`),
  KEY `idx_api_status` (`api_id`, `status`),
  KEY `idx_status_effective_to` (`status`, `effective_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用-API 授权';

-- T13 调用日志
DROP TABLE IF EXISTS `call_log`;
CREATE TABLE `call_log` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `trace_id`       VARCHAR(64)  NOT NULL,
  `api_id`         BIGINT       NOT NULL,
  `api_path`       VARCHAR(255) NOT NULL,
  `app_id`         BIGINT       NOT NULL,
  `client_ip`      VARCHAR(64)      NULL DEFAULT NULL,
  `request_params` JSON             NULL DEFAULT NULL,
  `response_size`  INT              NULL DEFAULT NULL,
  `record_count`   INT              NULL DEFAULT NULL,
  `http_status`    SMALLINT     NOT NULL,
  `biz_code`       INT              NULL DEFAULT NULL,
  `cost_ms`        INT          NOT NULL,
  `error_msg`      VARCHAR(512)     NULL DEFAULT NULL,
  `auth_type`      VARCHAR(16)      NULL DEFAULT NULL,
  `cache_hit`      TINYINT          NULL DEFAULT NULL,
  `stat_date`      DATE         NOT NULL,
  `created_at`     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_api_time` (`api_id`, `created_at`),
  KEY `idx_app_time` (`app_id`, `created_at`),
  KEY `idx_trace` (`trace_id`),
  KEY `idx_stat_date_api` (`stat_date`, `api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调用日志';

-- T14 日调用统计
DROP TABLE IF EXISTS `call_stat_daily`;
CREATE TABLE `call_stat_daily` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `stat_date`  DATE     NOT NULL,
  `api_id`     BIGINT   NOT NULL,
  `app_id`     BIGINT   NOT NULL DEFAULT 0,
  `total`      INT      NOT NULL DEFAULT 0,
  `success`    INT      NOT NULL DEFAULT 0,
  `fail`       INT      NOT NULL DEFAULT 0,
  `avg_cost`   INT      NOT NULL DEFAULT 0,
  `p95_cost`   INT      NOT NULL DEFAULT 0,
  `p99_cost`   INT      NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_api_app` (`stat_date`, `api_id`, `app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日调用统计';

-- T15 状态码分布统计
DROP TABLE IF EXISTS `call_stat_status`;
CREATE TABLE `call_stat_status` (
  `id`                BIGINT      NOT NULL AUTO_INCREMENT,
  `stat_date`         DATE        NOT NULL,
  `api_id`            BIGINT      NOT NULL DEFAULT 0,
  `app_id`            BIGINT      NOT NULL DEFAULT 0,
  `http_status_group` VARCHAR(8)  NOT NULL,
  `count`             INT         NOT NULL DEFAULT 0,
  `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_api_app_status` (`stat_date`, `api_id`, `app_id`, `http_status_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='状态码分布统计';

-- T16 告警规则
DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(128)  NOT NULL,
  `target_type` VARCHAR(16)   NOT NULL,
  `target_id`   BIGINT            NULL DEFAULT NULL,
  `metric`      VARCHAR(32)   NOT NULL,
  `operator`    VARCHAR(8)    NOT NULL,
  `threshold`   DECIMAL(18,4) NOT NULL,
  `window_sec`  INT           NOT NULL DEFAULT 60,
  `silence_sec` INT           NOT NULL DEFAULT 300,
  `channels`    VARCHAR(255)  NOT NULL DEFAULT 'INSITE',
  `enabled`     TINYINT       NOT NULL DEFAULT 1,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_target_enabled` (`target_type`, `target_id`, `enabled`),
  KEY `idx_metric_enabled` (`metric`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则';

-- T17 告警规则接收人
DROP TABLE IF EXISTS `alert_rule_receiver`;
CREATE TABLE `alert_rule_receiver` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT,
  `rule_id`    BIGINT   NOT NULL,
  `receiver`   BIGINT   NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_receiver` (`rule_id`, `receiver`),
  KEY `idx_receiver` (`receiver`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则接收人';

-- T18 告警事件
DROP TABLE IF EXISTS `alert_event`;
CREATE TABLE `alert_event` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `rule_id`       BIGINT        NOT NULL,
  `fired_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at`   DATETIME          NULL DEFAULT NULL,
  `status`        VARCHAR(16)   NOT NULL DEFAULT 'FIRING',
  `metric_value`  DECIMAL(18,4) NOT NULL,
  `message`       VARCHAR(512)      NULL DEFAULT NULL,
  `notify_status` VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
  `notify_msg`    VARCHAR(512)      NULL DEFAULT NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rule_status` (`rule_id`, `status`),
  KEY `idx_fired_at` (`fired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警事件';

-- T19 站内通知
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `receiver`   BIGINT       NOT NULL,
  `sender_id`  BIGINT           NULL DEFAULT NULL,
  `type`       VARCHAR(32)  NOT NULL,
  `title`      VARCHAR(128) NOT NULL,
  `content`    TEXT             NULL,
  `biz_type`   VARCHAR(32)      NULL DEFAULT NULL,
  `biz_id`     BIGINT           NULL DEFAULT NULL,
  `status`     VARCHAR(16)  NOT NULL DEFAULT 'UNREAD',
  `read_at`    DATETIME         NULL DEFAULT NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_receiver_status_type` (`receiver`, `status`, `type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';

-- T20 审计日志
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `operator`        BIGINT       NOT NULL,
  `action`          VARCHAR(32)  NOT NULL,
  `target_type`     VARCHAR(32)  NOT NULL,
  `target_id`       BIGINT       NOT NULL,
  `target_name`     VARCHAR(128)     NULL DEFAULT NULL,
  `before_snapshot` JSON             NULL DEFAULT NULL,
  `after_snapshot`  JSON             NULL DEFAULT NULL,
  `ip`              VARCHAR(64)      NULL DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator_time` (`operator`, `created_at`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';

SET FOREIGN_KEY_CHECKS = 1;
