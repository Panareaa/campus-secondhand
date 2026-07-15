-- campus_ai 库 DDL（2 张表）
-- 见 docs/数据库设计文档.md §七

USE campus_ai;

CREATE TABLE IF NOT EXISTS ai_prompt_log (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id   BIGINT UNSIGNED          DEFAULT NULL,
    scene        VARCHAR(32)     NOT NULL DEFAULT 'DESCRIBE',
    input_json   JSON            NOT NULL,
    output_json  JSON                     DEFAULT NULL,
    model_name   VARCHAR(64)     NOT NULL,
    latency_ms   INT             NOT NULL DEFAULT 0,
    status       TINYINT         NOT NULL DEFAULT 1 COMMENT '0=失败 1=成功 2=降级',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_account_created (account_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 描述生成日志';

CREATE TABLE IF NOT EXISTS ai_search_log (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id    BIGINT UNSIGNED          DEFAULT NULL,
    raw_query     VARCHAR(512)    NOT NULL,
    parsed_json   JSON                     DEFAULT NULL,
    result_count  INT             NOT NULL DEFAULT 0,
    latency_ms    INT             NOT NULL DEFAULT 0,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 语义搜索日志';
