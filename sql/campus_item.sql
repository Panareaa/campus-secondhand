-- campus_item 库 DDL（3 张表 + 分类种子数据）
-- 见 docs/数据库设计文档.md §四

USE campus_item;

CREATE TABLE IF NOT EXISTS item_category (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(64)     NOT NULL,
    parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0,
    sort_order  INT             NOT NULL DEFAULT 0,
    status      TINYINT         NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_sort (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类';

CREATE TABLE IF NOT EXISTS idle_item (
    id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    seller_id        BIGINT UNSIGNED NOT NULL COMMENT '逻辑引用 account',
    category_id      BIGINT UNSIGNED NOT NULL,
    title            VARCHAR(128)    NOT NULL,
    summary          VARCHAR(512)    NOT NULL DEFAULT '',
    description      TEXT                     DEFAULT NULL,
    condition_level  TINYINT         NOT NULL DEFAULT 3 COMMENT '成色 1~5',
    original_price   DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    sale_price       DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    status           TINYINT         NOT NULL DEFAULT 0 COMMENT '0=草稿 1=在售 2=已售 3=下架',
    view_count       INT UNSIGNED    NOT NULL DEFAULT 0,
    published_at     DATETIME                 DEFAULT NULL,
    deleted          TINYINT         NOT NULL DEFAULT 0,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_status (category_id, status, deleted),
    KEY idx_seller_status (seller_id, status, deleted),
    KEY idx_status_published (status, published_at DESC),
    KEY idx_title (title),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES item_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='闲置物品';

CREATE TABLE IF NOT EXISTS item_image (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    item_id     BIGINT UNSIGNED NOT NULL,
    image_url   VARCHAR(512)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    is_cover    TINYINT         NOT NULL DEFAULT 0 COMMENT '1=封面',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_item_sort (item_id, sort_order),
    CONSTRAINT fk_image_item FOREIGN KEY (item_id) REFERENCES idle_item (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品图片';

INSERT INTO item_category (id, name, parent_id, sort_order, status) VALUES
    (1, CONVERT(UNHEX('E69599E69D90E4B9A6E7B18D') USING utf8mb4), 0, 1, 1),
    (2, CONVERT(UNHEX('E695B0E7A081E794B5E5AD90') USING utf8mb4), 0, 2, 1),
    (3, CONVERT(UNHEX('E7949FE6B4BBE794A8E59381') USING utf8mb4), 0, 3, 1),
    (4, CONVERT(UNHEX('E8BF90E58AA8E688B7E5A496') USING utf8mb4), 0, 4, 1),
    (5, CONVERT(UNHEX('E585B6E4BB96E997B8E7BDAE') USING utf8mb4), 0, 5, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);
