-- campus_stock 库 DDL（2 张表）
-- 见 docs/数据库设计文档.md §六

USE campus_stock;

CREATE TABLE IF NOT EXISTS stock_record (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    item_id        BIGINT UNSIGNED NOT NULL COMMENT '逻辑引用 idle_item，一物一条',
    total_qty      INT UNSIGNED    NOT NULL DEFAULT 1,
    locked_qty     INT UNSIGNED    NOT NULL DEFAULT 0,
    available_qty  INT UNSIGNED    NOT NULL DEFAULT 1,
    version        INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '乐观锁',
    updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存主记录';

CREATE TABLE IF NOT EXISTS stock_lock_log (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    item_id      BIGINT UNSIGNED NOT NULL,
    trade_id     BIGINT UNSIGNED NOT NULL COMMENT '逻辑引用 trade_order',
    trade_no     VARCHAR(32)     NOT NULL,
    lock_qty     INT UNSIGNED    NOT NULL DEFAULT 1,
    lock_status  TINYINT         NOT NULL DEFAULT 1 COMMENT '1=锁定中 2=已释放',
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at  DATETIME                 DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_trade_item (trade_id, item_id),
    KEY idx_item_status (item_id, lock_status),
    KEY idx_trade_id (trade_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存锁定流水';
