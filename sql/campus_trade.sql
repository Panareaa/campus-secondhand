-- campus_trade 库 DDL（购物车 + 分片订单表 + 通知）
-- 见 docs/数据库设计文档.md §五
-- ShardingSphere 物理表: trade_order_0/_1, trade_line_0/_1

USE campus_trade;

CREATE TABLE IF NOT EXISTS cart_entry (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    buyer_id    BIGINT UNSIGNED NOT NULL COMMENT '逻辑引用 account',
    item_id     BIGINT UNSIGNED NOT NULL COMMENT '逻辑引用 idle_item',
    quantity    INT UNSIGNED    NOT NULL DEFAULT 1,
    item_title  VARCHAR(128)    NOT NULL DEFAULT '',
    item_price  DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    item_cover  VARCHAR(512)    NOT NULL DEFAULT '',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_buyer_item (buyer_id, item_id),
    KEY idx_buyer_updated (buyer_id, updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车';

CREATE TABLE IF NOT EXISTS trade_order_0 (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    trade_no        VARCHAR(32)     NOT NULL,
    buyer_id        BIGINT UNSIGNED NOT NULL COMMENT '分片键',
    seller_id       BIGINT UNSIGNED NOT NULL,
    total_amount    DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '0=待确认 1=已确认 2=已完成 3=已取消 4=已超时',
    buyer_contact   VARCHAR(128)    NOT NULL DEFAULT '',
    cancel_reason   VARCHAR(256)    NOT NULL DEFAULT '',
    confirmed_at    DATETIME                 DEFAULT NULL,
    completed_at    DATETIME                 DEFAULT NULL,
    expired_at      DATETIME                 DEFAULT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_trade_no (trade_no),
    KEY idx_buyer_status_created (buyer_id, status, created_at DESC),
    KEY idx_seller_status_created (seller_id, status, created_at DESC),
    KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易主单-分片0';

CREATE TABLE IF NOT EXISTS trade_order_1 LIKE trade_order_0;
ALTER TABLE trade_order_1 COMMENT='交易主单-分片1';

CREATE TABLE IF NOT EXISTS trade_line_0 (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    trade_id     BIGINT UNSIGNED NOT NULL,
    trade_no     VARCHAR(32)     NOT NULL,
    buyer_id     BIGINT UNSIGNED NOT NULL COMMENT '分片键',
    item_id      BIGINT UNSIGNED NOT NULL,
    seller_id    BIGINT UNSIGNED NOT NULL,
    item_title   VARCHAR(128)    NOT NULL,
    item_cover   VARCHAR(512)    NOT NULL DEFAULT '',
    unit_price   DECIMAL(10, 2)  NOT NULL,
    quantity     INT UNSIGNED    NOT NULL DEFAULT 1,
    line_amount  DECIMAL(10, 2)  NOT NULL,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trade_id (trade_id),
    KEY idx_buyer_created (buyer_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易明细-分片0';

CREATE TABLE IF NOT EXISTS trade_line_1 LIKE trade_line_0;
ALTER TABLE trade_line_1 COMMENT='交易明细-分片1';

CREATE TABLE IF NOT EXISTS trade_notify (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id   BIGINT UNSIGNED NOT NULL,
    trade_id     BIGINT UNSIGNED          DEFAULT NULL,
    trade_no     VARCHAR(32)     NOT NULL DEFAULT '',
    notify_type  VARCHAR(32)     NOT NULL COMMENT 'ORDER_CREATED/ORDER_CONFIRMED/ORDER_COMPLETED',
    title        VARCHAR(128)    NOT NULL,
    content      VARCHAR(512)    NOT NULL DEFAULT '',
    is_read      TINYINT         NOT NULL DEFAULT 0,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_account_read_created (account_id, is_read, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';
