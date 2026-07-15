-- campus_user 库 DDL（4 张表）
-- 见 docs/数据库设计文档.md §三

USE campus_user;

CREATE TABLE IF NOT EXISTS account (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    campus_id       VARCHAR(32)     NOT NULL COMMENT '学号/工号',
    login_name      VARCHAR(64)     NOT NULL COMMENT '登录名',
    password_hash   VARCHAR(128)    NOT NULL COMMENT 'BCrypt 密码哈希',
    nickname        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '昵称',
    contact_info    VARCHAR(128)    NOT NULL DEFAULT '' COMMENT '联系方式',
    avatar_url      VARCHAR(512)             DEFAULT NULL COMMENT '头像',
    role            TINYINT         NOT NULL DEFAULT 0 COMMENT '0=普通 1=管理员',
    cert_status     TINYINT         NOT NULL DEFAULT 0 COMMENT '0=未认证 1=已认证',
    reputation      INT             NOT NULL DEFAULT 0 COMMENT '信誉分',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '0=正常 1=锁定 2=注销',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_campus_id (campus_id),
    UNIQUE KEY uk_login_name (login_name),
    KEY idx_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园账号';

CREATE TABLE IF NOT EXISTS wishlist (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id  BIGINT UNSIGNED NOT NULL,
    item_id     BIGINT UNSIGNED NOT NULL COMMENT '逻辑引用 campus_item.idle_item',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_item (account_id, item_id),
    KEY idx_account_created (account_id, created_at DESC),
    CONSTRAINT fk_wishlist_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏';

CREATE TABLE IF NOT EXISTS point_account (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id  BIGINT UNSIGNED NOT NULL,
    balance     INT             NOT NULL DEFAULT 0,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_id (account_id),
    CONSTRAINT fk_point_account FOREIGN KEY (account_id) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分账户';

CREATE TABLE IF NOT EXISTS point_ledger (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id      BIGINT UNSIGNED NOT NULL,
    trade_id        BIGINT UNSIGNED          DEFAULT NULL COMMENT '逻辑引用 trade_order',
    change_amount   INT             NOT NULL,
    balance_after   INT             NOT NULL,
    rule_code       VARCHAR(32)     NOT NULL COMMENT 'TRADE_BUYER/TRADE_SELLER/REDEEM',
    idempotent_key  VARCHAR(64)     NOT NULL,
    remark          VARCHAR(256)    NOT NULL DEFAULT '',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotent_key (idempotent_key),
    KEY idx_account_created (account_id, created_at DESC),
    KEY idx_trade_id (trade_id),
    CONSTRAINT fk_point_ledger_account FOREIGN KEY (account_id) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水';
