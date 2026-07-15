-- 拾光校园 · 初始化全部逻辑库
-- 执行方式: mysql -u root -p < sql/00-init-databases.sql

CREATE DATABASE IF NOT EXISTS campus_user  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campus_item  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campus_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campus_stock DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS campus_ai    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
