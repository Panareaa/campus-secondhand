# 拾光校园 · REST API 接口规范

| 属性 | 内容 |
|------|------|
| **文档版本** | v1.1（本科简化版） |
| **撰写日期** | 2026-06-30 |
| **项目代号** | CampusRelife |
| **前置文档** | [PRD v0.3](./PRD-产品需求文档.md) · [架构设计 v1.0](./架构设计-系统总体架构.md) · [数据库设计 v1.0](./数据库设计文档.md) |
| **文档状态** | 已落地（对外 38 + 内部接口已实现，见各服务 Controller） |
| **Base URL** | `http://{host}/api`（经 Nginx 反代至 Gateway） |

---

## 一、通用约定

### 1.1 RESTful 规范

| 规则 | 说明 |
|------|------|
| 资源名用复数名词 | `/items`、`/trades`、`/categories` |
| URL 小写 + 连字符 | `/move-to-cart` |
| GET 查询 | 幂等，不改变状态 |
| POST 创建 | 非幂等（除带幂等键） |
| PUT 全量/动作更新 | 如确认、取消 |
| PATCH | 部分更新（本项目少用） |
| DELETE 删除 | 幂等 |
| 动作型接口 | `PUT /trades/{tradeNo}/confirm`，动词放末级 |

### 1.2 统一响应体 `ApiResponse<T>`

**成功：**

```json
{
  "code": 0,
  "message": "success",
  "data": { },
  "timestamp": 1719753600000,
  "traceId": "a1b2c3d4e5f6"
}
```

**失败：**

```json
{
  "code": 30002,
  "message": "库存不足",
  "data": null,
  "timestamp": 1719753600000,
  "traceId": "a1b2c3d4e5f6"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | `0` = 成功；非 0 = 业务/系统错误码 |
| `message` | string | 人类可读描述 |
| `data` | T / null | 业务数据 |
| `timestamp` | long | 毫秒时间戳 |
| `traceId` | string | 链路追踪 ID，Gateway 生成 |

### 1.3 HTTP 状态码与业务码关系

| HTTP Status | 使用场景 |
|-------------|----------|
| 200 | 成功（`code=0`） |
| 400 | 参数校验失败（`code=400xx`） |
| 401 | 未登录 / Token 无效（`code=401xx`） |
| 403 | 无权限（`code=403xx`） |
| 404 | 资源不存在（`code=404xx`） |
| 409 | 冲突：重复提交、状态不允许（`code=409xx`） |
| 422 | 业务规则不满足（`code=422xx`） |
| 429 | 限流（`code=42900`） |
| 500 | 服务器内部错误（`code=50000`） |

> **约定：** HTTP 状态码表达传输层语义；`body.code` 表达精确业务错误，前端以 `code` 为准。

### 1.4 分页请求 `PageQuery`

**Query 参数：**

| 参数 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `page` | int | 1 | 页码，从 1 开始 |
| `size` | int | 10 | 每页条数，最大 50 |

**分页响应 `PageResult<T>`：**

```json
{
  "list": [],
  "page": 1,
  "size": 10,
  "total": 100,
  "pages": 10
}
```

### 1.5 公共请求头

| Header | 必填 | 说明 |
|--------|------|------|
| `Authorization` | 鉴权接口必填 | `Bearer {accessToken}` |
| `Content-Type` | 有 Body 时必填 | `application/json` |
| `Idempotency-Key` | 幂等接口必填 | UUID，见第十章 |
| `X-Trace-Id` | 否 | 客户端可传；无则 Gateway 生成 |

### 1.6 Gateway 透传头（内部）

Gateway 校验 JWT 后向下游注入（**客户端不传**）：

| Header | 说明 |
|--------|------|
| `X-User-Id` | 当前用户 ID |
| `X-User-Role` | `USER` / `ADMIN` |
| `X-Trace-Id` | 链路 ID |

---

## 二、JWT 设计

### 2.1 Token 类型

| 类型 | 有效期 | 用途 |
|------|--------|------|
| `accessToken` | 2 小时 | API 访问 |
| `refreshToken` | 7 天 | 刷新 accessToken（P1 可选） |

### 2.2 Access Token Payload（Claims）

```json
{
  "sub": "10001",
  "loginName": "zhang@ynu.edu.cn",
  "campusId": "2021001001",
  "role": "USER",
  "jti": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "iat": 1719753600,
  "exp": 1719760800
}
```

| Claim | 说明 |
|-------|------|
| `sub` | 用户 ID（`account.id`） |
| `loginName` | 登录名 |
| `campusId` | 学号/工号 |
| `role` | `USER` 或 `ADMIN` |
| `jti` | Token 唯一 ID，登出加入 Redis 黑名单 |
| `iat` / `exp` | 签发 / 过期时间（秒） |

### 2.3 签名算法

- **算法：** HS256（课程演示）或 RS256（生产推荐）
- **密钥：** 存 Nacos `campus-user-service.yaml` → `jwt.secret`
- **签发方：** `campus-user-service`
- **校验方：** `campus-gateway`（本地校验，避免每次请求 User）

### 2.4 鉴权方式总表

| 接口类型 | 鉴权 |
|----------|------|
| 公开读接口 | 无需 Token |
| 用户写接口 | `Authorization: Bearer` + Gateway 校验 |
| 管理员接口 | Token + `role=ADMIN` |
| 服务间 Feign | `/internal/**` 不经过 Gateway；`X-Internal-Token` 服务密钥 |
| 健康检查 | `/actuator/health` 内网开放 |

### 2.5 Gateway 白名单（无需 JWT）

```
POST /api/auth/register
POST /api/auth/login
GET  /api/items
GET  /api/items/{id}
GET  /api/categories
GET  /api/categories/{id}
GET  /actuator/health
```

### 2.6 登出

```
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

将 `jti` 写入 Redis `auth:blacklist:{jti}`，TTL = Token 剩余有效期。

---

## 三、全局错误码

### 3.1 错误码分段

| 段位 | 模块 | 示例 |
|------|------|------|
| `0` | 成功 | — |
| `40001-40099` | 通用参数 | 参数缺失 |
| `40101-40199` | 认证 | Token 过期 |
| `40301-40399` | 权限 | 非本人资源 |
| `40401-40499` | 资源不存在 | 物品不存在 |
| `40901-40999` | 冲突 | 重复收藏 |
| `42201-42299` | 业务规则 | 库存不足 |
| `42900` | 限流 | 请求过于频繁 |
| `50000-50099` | 系统 | 内部错误 |
| `1xxxx` | User 域 | 用户锁定 |
| `2xxxx` | Item 域 | 物品已下架 |
| `3xxxx` | Trade 域 | 订单状态错误 |
| `4xxxx` | Stock 域 | 锁定失败 |
| `5xxxx` | AI 域 | AI 服务降级 |

### 3.2 错误码清单

| code | HTTP | message | 说明 |
|------|------|---------|------|
| 0 | 200 | success | 成功 |
| 40001 | 400 | 参数校验失败 | Bean Validation |
| 40002 | 400 | 分页参数非法 | page/size 越界 |
| 40101 | 401 | 未登录 | 无 Token |
| 40102 | 401 | Token 无效 | 签名错误 |
| 40103 | 401 | Token 已过期 | exp 超时 |
| 40104 | 401 | Token 已失效 | 登出黑名单 |
| 40301 | 403 | 无权限访问 | 角色不足 |
| 40302 | 403 | 无权操作他人资源 | 非本人 |
| 40401 | 404 | 资源不存在 | 通用 |
| 40901 | 409 | 重复请求 | 幂等键冲突 |
| 40902 | 409 | 资源已存在 | 重复收藏 |
| 42201 | 422 | 联系方式未设置 | 下单前必填 |
| 42202 | 422 | 购物车为空 | 结算 |
| 42203 | 422 | 物品非在售状态 | 加购/下单 |
| 42204 | 422 | 库存不足 | 加购/下单 |
| 42205 | 422 | 交易状态不允许此操作 | 状态机 |
| 42206 | 422 | 不能购买自己的物品 | 业务规则 |
| 42900 | 429 | 请求过于频繁 | 限流 |
| 50000 | 500 | 系统内部错误 | 未捕获异常 |
| 50001 | 500 | 下游服务不可用 | Feign 熔断 |
| 10001 | 401 | 用户名或密码错误 | 登录 |
| 10002 | 403 | 账号已锁定 | 登录 |
| 10003 | 409 | 登录名已存在 | 注册 |
| 10004 | 409 | 学号已注册 | 注册 |
| 20001 | 404 | 物品不存在 | — |
| 20002 | 422 | 物品已下架或已售出 | — |
| 20003 | 403 | 非物品发布者 | 编辑/下架 |
| 30001 | 404 | 交易单不存在 | — |
| 30002 | 422 | 交易状态不允许操作 | — |
| 41001 | 422 | 库存锁定失败 | Stock |
| 41002 | 422 | 库存记录不存在 | Stock |
| 50001 | 503 | AI 服务暂时不可用 | 降级 |
| 50002 | 200 | AI 已降级返回模板 | 降级成功仍 code=0，data 标记 degraded |

---

## 四、认证模块（User Service）

### 4.1 用户注册

| 项 | 内容 |
|----|------|
| **URL** | `/api/auth/register` |
| **Method** | `POST` |
| **鉴权** | 公开 |

**Request：**

```json
{
  "campusId": "2021001001",
  "loginName": "zhang@ynu.edu.cn",
  "password": "Passw0rd!",
  "nickname": "小张",
  "contactInfo": "13800001111"
}
```

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| campusId | string | 是 | 6-32 位 |
| loginName | string | 是 | 邮箱或手机号 |
| password | string | 是 | 8-32 位 |
| nickname | string | 是 | 1-64 位 |
| contactInfo | string | 否 | 0-128 位 |

**Response `data`：**

```json
{
  "userId": 10001,
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 7200,
  "tokenType": "Bearer"
}
```

**错误码：** `10003` `10004` `40001`

---

### 4.2 用户登录

| 项 | 内容 |
|----|------|
| **URL** | `/api/auth/login` |
| **Method** | `POST` |
| **鉴权** | 公开 |

**Request：**

```json
{
  "loginName": "zhang@ynu.edu.cn",
  "password": "Passw0rd!"
}
```

**Response `data`：** 同注册

**错误码：** `10001` `10002` `40001`

---

### 4.3 用户登出

| 项 | 内容 |
|----|------|
| **URL** | `/api/auth/logout` |
| **Method** | `POST` |
| **鉴权** | Bearer JWT |

**Request：** 无 Body

**Response `data`：** `null`

---

## 五、用户模块（User Service）

### 5.1 获取当前用户信息

| 项 | 内容 |
|----|------|
| **URL** | `/api/users/me` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Response `data`：**

```json
{
  "userId": 10001,
  "campusId": "2021001001",
  "loginName": "zhang@ynu.edu.cn",
  "nickname": "小张",
  "contactInfo": "13800001111",
  "avatarUrl": "https://cdn.example/avatar.png",
  "role": "USER",
  "certStatus": 1,
  "reputation": 100,
  "greenPoints": 50,
  "createdAt": "2026-06-01T10:00:00"
}
```

---

### 5.2 更新个人资料

| 项 | 内容 |
|----|------|
| **URL** | `/api/users/me` |
| **Method** | `PUT` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "nickname": "新昵称",
  "avatarUrl": "https://cdn.example/new.png"
}
```

**Response `data`：** 同 5.1

---

### 5.3 更新联系方式

| 项 | 内容 |
|----|------|
| **URL** | `/api/users/me/contact` |
| **Method** | `PUT` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "contactInfo": "微信: zhang_ynu"
}
```

**错误码：** `40001`

---

### 5.4 校园认证

| 项 | 内容 |
|----|------|
| **URL** | `/api/users/me/certification` |
| **Method** | `POST` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "realName": "张三"
}
```

**Response `data`：**

```json
{
  "certStatus": 1
}
```

---

## 六、收藏模块（User Service）

### 6.1 收藏列表

| 项 | 内容 |
|----|------|
| **URL** | `/api/wishlist` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Query：** `page` `size`

**Response `data`：** `PageResult<WishlistVO>`

```json
{
  "list": [
    {
      "wishlistId": 1,
      "itemId": 20001,
      "itemTitle": "九成新 iPad",
      "itemCover": "https://cdn/cover.jpg",
      "salePrice": 1500.00,
      "itemStatus": 1,
      "valid": true,
      "createdAt": "2026-06-10T12:00:00"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 5,
  "pages": 1
}
```

> `valid=false` 表示物品已下架/售出，收藏仍保留。

---

### 6.2 添加收藏

| 项 | 内容 |
|----|------|
| **URL** | `/api/wishlist` |
| **Method** | `POST` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "itemId": 20001
}
```

**Response `data`：**

```json
{
  "wishlistId": 1,
  "itemId": 20001
}
```

**错误码：** `40902` `20001` `42203`

---

### 6.3 取消收藏

| 项 | 内容 |
|----|------|
| **URL** | `/api/wishlist/{itemId}` |
| **Method** | `DELETE` |
| **鉴权** | Bearer |

**Response `data`：** `null`

---

### 6.4 收藏移入购物车

| 项 | 内容 |
|----|------|
| **URL** | `/api/wishlist/{itemId}/move-to-cart` |
| **Method** | `POST` |
| **鉴权** | Bearer |

**Request：** 无 Body（可选 `"keepWishlist": true`）

**Response `data`：**

```json
{
  "cartEntryId": 10,
  "itemId": 20001
}
```

**说明：** User 服务接收请求 → Feign 调 Trade `POST /internal/cart/items`

**错误码：** `20002` `42204` `42206`

---

## 七、积分模块（User Service）

### 7.1 我的积分

| 项 | 内容 |
|----|------|
| **URL** | `/api/points/me` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Response `data`：**

```json
{
  "balance": 50,
  "updatedAt": "2026-06-15T08:00:00"
}
```

---

### 7.2 积分流水

| 项 | 内容 |
|----|------|
| **URL** | `/api/points/me/ledger` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Query：** `page` `size`

**Response `data`：** `PageResult<PointLedgerVO>`

```json
{
  "list": [
    {
      "ledgerId": 1,
      "changeAmount": 10,
      "balanceAfter": 50,
      "ruleCode": "TRADE_BUYER",
      "remark": "交易完成奖励",
      "tradeId": 30001,
      "createdAt": "2026-06-15T08:00:00"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 3,
  "pages": 1
}
```

---

## 八、分类模块（Item Service）

### 8.1 分类列表

| 项 | 内容 |
|----|------|
| **URL** | `/api/categories` |
| **Method** | `GET` |
| **鉴权** | 公开 |

**Query：** `parentId`（可选，默认 0 顶级）

**Response `data`：**

```json
[
  {
    "categoryId": 1,
    "name": "书籍教材",
    "parentId": 0,
    "sortOrder": 1
  }
]
```

---

### 8.2 分类详情

| 项 | 内容 |
|----|------|
| **URL** | `/api/categories/{categoryId}` |
| **Method** | `GET` |
| **鉴权** | 公开 |

---

### 8.3 创建分类（管理员）

| 项 | 内容 |
|----|------|
| **URL** | `/api/categories` |
| **Method** | `POST` |
| **鉴权** | Bearer + ADMIN |

**Request：**

```json
{
  "name": "运动户外",
  "parentId": 0,
  "sortOrder": 5
}
```

---

### 8.4 更新 / 删除分类（管理员）

| URL | Method | 鉴权 |
|-----|--------|------|
| `/api/categories/{categoryId}` | `PUT` | ADMIN |
| `/api/categories/{categoryId}` | `DELETE` | ADMIN |

---

## 九、物品模块（Item Service）

### 9.1 物品列表 / 搜索

| 项 | 内容 |
|----|------|
| **URL** | `/api/items` |
| **Method** | `GET` |
| **鉴权** | 公开 |

**Query：**

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页 |
| categoryId | long | 分类筛选 |
| keyword | string | 关键词 |
| minPrice | decimal | 最低价 |
| maxPrice | decimal | 最高价 |
| conditionLevel | int | 成色 |
| sort | string | `latest` / `price_asc` / `price_desc` |
| status | int | 默认 1（在售），管理可查全部 |

**Response `data`：** `PageResult<ItemSummaryVO>`

```json
{
  "list": [
    {
      "itemId": 20001,
      "title": "九成新 iPad",
      "summary": "考研自用",
      "coverUrl": "https://cdn/cover.jpg",
      "salePrice": 1500.00,
      "conditionLevel": 4,
      "categoryId": 2,
      "categoryName": "数码电子",
      "sellerId": 10002,
      "status": 1,
      "publishedAt": "2026-06-10T10:00:00"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 100,
  "pages": 10
}
```

---

### 9.2 物品详情

| 项 | 内容 |
|----|------|
| **URL** | `/api/items/{itemId}` |
| **Method** | `GET` |
| **鉴权** | 公开 |

**Response `data`：**

```json
{
  "itemId": 20001,
  "sellerId": 10002,
  "sellerNickname": "小李",
  "categoryId": 2,
  "categoryName": "数码电子",
  "title": "九成新 iPad",
  "summary": "考研自用",
  "description": "详细描述...",
  "conditionLevel": 4,
  "originalPrice": 3000.00,
  "salePrice": 1500.00,
  "status": 1,
  "viewCount": 128,
  "images": [
    { "imageUrl": "https://cdn/1.jpg", "isCover": true, "sortOrder": 0 }
  ],
  "availableQty": 1,
  "publishedAt": "2026-06-10T10:00:00",
  "createdAt": "2026-06-10T09:00:00"
}
```

> `availableQty` 由 Item 服务 Feign 查 Stock（或详情聚合时调用）。

**错误码：** `20001`

---

### 9.3 发布物品

| 项 | 内容 |
|----|------|
| **URL** | `/api/items` |
| **Method** | `POST` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "categoryId": 2,
  "title": "九成新 iPad",
  "summary": "考研自用",
  "description": "详细描述...",
  "conditionLevel": 4,
  "originalPrice": 3000.00,
  "salePrice": 1500.00,
  "images": [
    { "imageUrl": "https://cdn/1.jpg", "isCover": true, "sortOrder": 0 }
  ],
  "publish": true
}
```

**Response `data`：**

```json
{
  "itemId": 20001,
  "status": 1
}
```

**说明：** `publish=true` 时上架并 Feign 调 Stock 初始化库存。

---

### 9.4 编辑物品

| 项 | 内容 |
|----|------|
| **URL** | `/api/items/{itemId}` |
| **Method** | `PUT` |
| **鉴权** | Bearer（本人或 ADMIN） |

**Request：** 同 9.3（不含 publish）

**错误码：** `20003` `20001`

---

### 9.5 上架 / 下架

| URL | Method | 说明 |
|-----|--------|------|
| `/api/items/{itemId}/publish` | `PUT` | 草稿/下架 → 在售 |
| `/api/items/{itemId}/off-shelf` | `PUT` | 在售 → 下架 |

**Response `data`：**

```json
{
  "itemId": 20001,
  "status": 1
}
```

---

### 9.6 删除物品

| 项 | 内容 |
|----|------|
| **URL** | `/api/items/{itemId}` |
| **Method** | `DELETE` |
| **鉴权** | Bearer（本人） |

软删除。

---

### 9.7 我的发布

| 项 | 内容 |
|----|------|
| **URL** | `/api/items/mine` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Query：** `page` `size` `status`

**Response `data`：** `PageResult<ItemSummaryVO>`

---

## 十、购物车模块（Trade Service）

### 10.1 购物车列表

| 项 | 内容 |
|----|------|
| **URL** | `/api/cart` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Response `data`：**

```json
{
  "items": [
    {
      "cartEntryId": 10,
      "itemId": 20001,
      "itemTitle": "九成新 iPad",
      "itemCover": "https://cdn/cover.jpg",
      "itemPrice": 1500.00,
      "quantity": 1,
      "lineAmount": 1500.00,
      "valid": true,
      "invalidReason": null
    }
  ],
  "totalAmount": 1500.00,
  "totalQuantity": 1
}
```

---

### 10.2 加入购物车

| 项 | 内容 |
|----|------|
| **URL** | `/api/cart/items` |
| **Method** | `POST` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "itemId": 20001,
  "quantity": 1
}
```

**Response `data`：**

```json
{
  "cartEntryId": 10,
  "itemId": 20001,
  "quantity": 1
}
```

**错误码：** `20002` `42204` `42206` `40902`（已在购物车则合并数量）

---

### 10.3 修改数量

| 项 | 内容 |
|----|------|
| **URL** | `/api/cart/items/{itemId}` |
| **Method** | `PUT` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "quantity": 1
}
```

---

### 10.4 删除购物车项

| 项 | 内容 |
|----|------|
| **URL** | `/api/cart/items/{itemId}` |
| **Method** | `DELETE` |
| **鉴权** | Bearer |

---

### 10.5 清空购物车

| 项 | 内容 |
|----|------|
| **URL** | `/api/cart` |
| **Method** | `DELETE` |
| **鉴权** | Bearer |

---

### 10.6 结算预览

| 项 | 内容 |
|----|------|
| **URL** | `/api/cart/settle-preview` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Response `data`：**

```json
{
  "items": [ ],
  "totalAmount": 1500.00,
  "contactInfo": "13800001111",
  "contactReady": true
}
```

**错误码：** `42201` `42202`

---

## 十一、交易模块（Trade Service）

### 11.1 购物车结算下单 ⭐

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/checkout` |
| **Method** | `POST` |
| **鉴权** | Bearer |
| **幂等** | **必填** `Idempotency-Key: {UUID}` |

**Request：**

```json
{
  "remark": "希望周末交易"
}
```

**Response `data`：**

```json
{
  "tradeNo": "T20260630103000123456",
  "tradeId": 30001,
  "totalAmount": 1500.00,
  "status": 0,
  "statusText": "PENDING",
  "lines": [
    {
      "itemId": 20001,
      "itemTitle": "九成新 iPad",
      "unitPrice": 1500.00,
      "quantity": 1,
      "lineAmount": 1500.00
    }
  ],
  "createdAt": "2026-06-30T10:30:00"
}
```

**错误码：** `42201` `42202` `42203` `42204` `40901` `40001` `50001`

---

### 11.2 立即购买（单件）

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/direct` |
| **Method** | `POST` |
| **鉴权** | Bearer |
| **幂等** | **必填** `Idempotency-Key` |

**Request：**

```json
{
  "itemId": 20001,
  "quantity": 1,
  "remark": ""
}
```

**Response `data`：** 同 11.1

---

### 11.3 我买到的订单

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/buyer` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Query：** `page` `size` `status`

---

### 11.4 我卖出的订单

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/seller` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Query：** `page` `size` `status`

**Response `data`：** `PageResult<TradeSummaryVO>`

```json
{
  "list": [
    {
      "tradeNo": "T20260630103000123456",
      "tradeId": 30001,
      "buyerId": 10001,
      "sellerId": 10002,
      "totalAmount": 1500.00,
      "status": 0,
      "statusText": "PENDING",
      "itemCount": 1,
      "coverUrl": "https://cdn/cover.jpg",
      "createdAt": "2026-06-30T10:30:00"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 2,
  "pages": 1
}
```

---

### 11.5 交易详情

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/{tradeNo}` |
| **Method** | `GET` |
| **鉴权** | Bearer（买家/卖家/ADMIN） |

**Response `data`：**

```json
{
  "tradeNo": "T20260630103000123456",
  "tradeId": 30001,
  "buyerId": 10001,
  "sellerId": 10002,
  "buyerContact": "13800001111",
  "totalAmount": 1500.00,
  "status": 1,
  "statusText": "CONFIRMED",
  "remark": "",
  "lines": [
    {
      "lineId": 1,
      "itemId": 20001,
      "itemTitle": "九成新 iPad",
      "itemCover": "https://cdn/cover.jpg",
      "unitPrice": 1500.00,
      "quantity": 1,
      "lineAmount": 1500.00
    }
  ],
  "confirmedAt": "2026-06-30T11:00:00",
  "completedAt": null,
  "createdAt": "2026-06-30T10:30:00"
}
```

**错误码：** `30001` `40302`

---

### 11.6 卖家确认

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/{tradeNo}/confirm` |
| **Method** | `PUT` |
| **鉴权** | Bearer（卖家） |

**Request：** 无 Body 或 `{}`

**状态流转：** `PENDING(0)` → `CONFIRMED(1)`

**错误码：** `30002` `40302`

---

### 11.7 买家确认收货

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/{tradeNo}/complete` |
| **Method** | `PUT` |
| **鉴权** | Bearer（买家） |

**状态流转：** `CONFIRMED(1)` → `COMPLETED(2)`

**副作用：** 同步 Feign 调 User 发积分（买卖双方各一次，幂等键防重复）

**错误码：** `30002` `40302`

---

### 11.8 取消交易

| 项 | 内容 |
|----|------|
| **URL** | `/api/trades/{tradeNo}/cancel` |
| **Method** | `PUT` |
| **鉴权** | Bearer（买家/卖家，规则见下） |

**Request：**

```json
{
  "reason": "不想买了"
}
```

**规则：**
- `PENDING`：买家、卖家均可取消
- `CONFIRMED`：仅卖家可取消（或双方协商扩展）

**状态流转：** → `CANCELLED(3)`，Feign 释放库存

**错误码：** `30002`

---

### 11.9 交易状态枚举

| status | statusText | 说明 |
|--------|------------|------|
| 0 | PENDING | 待卖家确认 |
| 1 | CONFIRMED | 待买家确认收货 |
| 2 | COMPLETED | 已完成 |
| 3 | CANCELLED | 已取消 |
| 4 | EXPIRED | 已超时（系统） |

---

## 十二、通知模块（Trade Service）

### 12.1 通知列表

| 项 | 内容 |
|----|------|
| **URL** | `/api/notifications` |
| **Method** | `GET` |
| **鉴权** | Bearer |

**Query：** `page` `size` `isRead`

**Response `data`：** `PageResult<NotificationVO>`

```json
{
  "list": [
    {
      "notificationId": 1,
      "notifyType": "ORDER_CREATED",
      "title": "您有新的交易请求",
      "content": "买家下单：九成新 iPad",
      "tradeNo": "T20260630103000123456",
      "isRead": false,
      "createdAt": "2026-06-30T10:30:00"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 5,
  "pages": 1
}
```

---

### 12.2 标记已读

| URL | Method |
|-----|--------|
| `/api/notifications/{notificationId}/read` | `PUT` |
| `/api/notifications/read-all` | `PUT` |

---

## 十三、AI 模块（AI Service）

### 13.1 闲置描述生成

| 项 | 内容 |
|----|------|
| **URL** | `/api/ai/describe` |
| **Method** | `POST` |
| **鉴权** | Bearer |

**Request：**

```json
{
  "title": "九成新 iPad",
  "categoryName": "数码电子",
  "conditionLevel": 4,
  "keywords": ["考研", "笔记"],
  "originalPrice": 3000
}
```

**Response `data`：**

```json
{
  "description": "这款 iPad 购于2024年，日常用于考研网课……",
  "suggestedTags": ["考研", "平板", "九成新"],
  "degraded": false,
  "modelName": "gpt-4o-mini"
}
```

**错误码：** `50001`（熔断）；降级时 `code=0`，`degraded=true`

---

### 13.2 语义搜索

| 项 | 内容 |
|----|------|
| **URL** | `/api/ai/search` |
| **Method** | `POST` |
| **鉴权** | Bearer（公开可读可改为可选登录） |

**Request：**

```json
{
  "query": "宿舍用的小台灯，便宜点的",
  "page": 1,
  "size": 10
}
```

**Response `data`：**

```json
{
  "parsedCondition": {
    "categoryId": 3,
    "keywords": ["台灯", "宿舍"],
    "maxPrice": 50
  },
  "items": { },
  "degraded": false
}
```

> `items` 结构与 `PageResult<ItemSummaryVO>` 相同，由 AI 服务 Feign Item 后聚合返回。

---

## 十四、Feign 内部接口

> **约定：**
> - 路径前缀 `/internal`
> - **不经过 Gateway**，不对外暴露
> - 鉴权：`X-Internal-Token: {serviceSecret}`（Nacos 配置）
> - 响应体统一 `ApiResponse<T>`

### 14.1 User Service 内部接口

#### 14.1.1 校验用户有效性

| 项 | 内容 |
|----|------|
| **URL** | `/internal/users/{userId}/validate` |
| **Method** | `GET` |
| **调用方** | Trade |

**Response `data`：**

```json
{
  "valid": true,
  "status": 0,
  "contactInfo": "13800001111",
  "contactReady": true
}
```

---

#### 14.1.2 批量查询用户昵称

| 项 | 内容 |
|----|------|
| **URL** | `/internal/users/batch` |
| **Method** | `POST` |

**Request：**

```json
{
  "userIds": [10001, 10002]
}
```

**Response `data`：**

```json
{
  "10001": { "nickname": "小张", "reputation": 100 },
  "10002": { "nickname": "小李", "reputation": 80 }
}
```

---

#### 14.1.3 发放积分（幂等）

| 项 | 内容 |
|----|------|
| **URL** | `/internal/points/grant` |
| **Method** | `POST` |
| **调用方** | Trade（确认收货时同步调用） |
| **幂等** | Body `idempotentKey` |

**Request：**

```json
{
  "accountId": 10001,
  "tradeId": 30001,
  "changeAmount": 10,
  "ruleCode": "TRADE_BUYER",
  "idempotentKey": "30001_TRADE_BUYER",
  "remark": "交易完成奖励"
}
```

**Response `data`：**

```json
{
  "ledgerId": 1,
  "balanceAfter": 60,
  "duplicate": false
}
```

> `duplicate=true` 表示幂等键已处理，返回已有结果。

---

#### 14.1.4 ~~更新信誉~~（MVP 不做）

> 信誉流水表已移出 MVP。`account.reputation` 字段可保留默认值，二期再实现变更接口。

### 14.2 Item Service 内部接口

#### 14.2.1 在售校验（单个）

| 项 | 内容 |
|----|------|
| **URL** | `/internal/items/{itemId}/sale-check` |
| **Method** | `GET` |
| **调用方** | Trade、Stock |

**Response `data`：**

```json
{
  "itemId": 20001,
  "onSale": true,
  "sellerId": 10002,
  "title": "九成新 iPad",
  "coverUrl": "https://cdn/cover.jpg",
  "salePrice": 1500.00,
  "status": 1
}
```

---

#### 14.2.2 在售校验（批量）

| 项 | 内容 |
|----|------|
| **URL** | `/internal/items/sale-check/batch` |
| **Method** | `POST` |
| **调用方** | Trade |

**Request：**

```json
{
  "itemIds": [20001, 20002]
}
```

---

#### 14.2.3 结构化条件搜索

| 项 | 内容 |
|----|------|
| **URL** | `/internal/items/search` |
| **Method** | `POST` |
| **调用方** | AI |

**Request：**

```json
{
  "categoryId": 3,
  "keywords": ["台灯", "宿舍"],
  "minPrice": null,
  "maxPrice": 50,
  "page": 1,
  "size": 10,
  "sort": "price_asc"
}
```

**Response `data`：** `PageResult<ItemSummaryVO>`

---

#### 14.2.4 标记物品已售

| 项 | 内容 |
|----|------|
| **URL** | `/internal/items/{itemId}/sold` |
| **Method** | `PUT` |
| **调用方** | Trade（订单完成后） |

---

### 14.3 Stock Service 内部接口

#### 14.3.1 初始化库存

| 项 | 内容 |
|----|------|
| **URL** | `/internal/stock/init` |
| **Method** | `POST` |
| **调用方** | Item |
| **幂等** | `itemId` 唯一，重复调用返回已有记录 |

**Request：**

```json
{
  "itemId": 20001,
  "totalQty": 1
}
```

---

#### 14.3.2 查询可售库存

| 项 | 内容 |
|----|------|
| **URL** | `/internal/stock/{itemId}/available` |
| **Method** | `GET` |
| **调用方** | Trade、Item |

**Response `data`：**

```json
{
  "itemId": 20001,
  "totalQty": 1,
  "lockedQty": 0,
  "availableQty": 1
}
```

---

#### 14.3.3 锁定库存（幂等）

| 项 | 内容 |
|----|------|
| **URL** | `/internal/stock/lock` |
| **Method** | `POST` |
| **调用方** | Trade |
| **幂等** | `tradeId + itemId` 唯一 |

**Request：**

```json
{
  "itemId": 20001,
  "tradeId": 30001,
  "tradeNo": "T20260630103000123456",
  "quantity": 1
}
```

**Response `data`：**

```json
{
  "lockLogId": 1,
  "success": true,
  "duplicate": false
}
```

**错误码：** `42204` `41001`

---

#### 14.3.4 释放库存（幂等）

| 项 | 内容 |
|----|------|
| **URL** | `/internal/stock/release` |
| **Method** | `POST` |
| **调用方** | Trade |
| **幂等** | `tradeId + itemId` |

**Request：**

```json
{
  "itemId": 20001,
  "tradeId": 30001
}
```

---

### 14.4 Trade Service 内部接口

#### 14.4.1 内部加购（收藏移入）

| 项 | 内容 |
|----|------|
| **URL** | `/internal/cart/items` |
| **Method** | `POST` |
| **调用方** | User |

**Request：**

```json
{
  "buyerId": 10001,
  "itemId": 20001,
  "quantity": 1
}
```

---

### 14.5 Feign 接口汇总表

| 调用方 | 被调方 | 接口 | 场景 |
|--------|--------|------|------|
| Trade | User | `GET /internal/users/{id}/validate` | 下单校验 |
| Trade | User | `POST /internal/points/grant` | 确认收货同步发积分 |
| Trade | Item | `GET /internal/items/{id}/sale-check` | 下单校验 |
| Trade | Item | `POST /internal/items/sale-check/batch` | 批量校验 |
| Trade | Item | `PUT /internal/items/{id}/sold` | 完成后标记已售 |
| Trade | Stock | `POST /internal/stock/lock` | 下单锁库存 |
| Trade | Stock | `POST /internal/stock/release` | 取消释放 |
| Item | Stock | `POST /internal/stock/init` | 发布初始化 |
| Item | Stock | `GET /internal/stock/{id}/available` | 详情展示 |
| AI | Item | `POST /internal/items/search` | 语义搜索结果 |
| User | Trade | `POST /internal/cart/items` | 收藏移入购物车 |

---

## 十五、幂等设计

### 15.1 幂等场景总表

| 场景 | 机制 | Key 组成 | 存储 | TTL |
|------|------|----------|------|-----|
| 购物车结算下单 | 请求头 | `Idempotency-Key: UUID` | Redis `trade:idempotent:{userId}:{key}` | 5 min |
| 立即购买 | 请求头 | 同上 | 同上 | 5 min |
| 库存锁定 | 业务唯一 | `tradeId + itemId` | DB `uk_trade_item` + Redis 锁 | — |
| 库存释放 | 业务唯一 | `tradeId + itemId` | 已释放则直接返回成功 | — |
| 积分发放 | Body 字段 | `{tradeId}_{ruleCode}` | DB `uk_idempotent_key` | 永久 |
| 信誉变更 | Body 字段 | `{tradeId}_REP` | DB 唯一索引 | 永久 |
| 库存初始化 | 资源唯一 | `itemId` | DB `uk_item_id` | 永久 |
| 添加收藏 | 业务唯一 | `accountId + itemId` | DB `uk_account_item` | 永久 |

### 15.2 下单幂等流程

```
1. 客户端生成 UUID → Header: Idempotency-Key
2. Gateway 透传至 Trade
3. Trade: SETNX trade:idempotent:{userId}:{key}
   ├─ 失败 → 返回 40901 或返回首次结果（推荐返回首次 tradeNo）
   └─ 成功 → 继续下单流程
4. 下单完成 → Redis 值写入 tradeNo（供重复请求返回）
5. TTL 5 分钟后 Key 过期（订单已落库，不再依赖 Redis）
```

### 15.3 积分发放（同步 Feign，非 Outbox）

```
1. 买家点击「确认收货」→ Trade 更新状态 COMPLETED
2. Trade 同步 Feign → User POST /internal/points/grant（买家、卖家各调一次）
3. User：INSERT point_ledger (idempotent_key=...)
   ├─ 唯一键冲突 → 返回 duplicate=true，不重复加分
   └─ 成功 → 更新 point_account.balance
4. 若 Feign 失败 → 接口返回 500，用户可再次点击；幂等键保证安全
```

### 15.4 库存锁幂等流程

```
1. Trade 调 POST /internal/stock/lock
2. Stock：uk_trade_item 已存在且状态=锁定中 → 返回 duplicate=true
3. 否则：Redis 锁 itemId → UPDATE stock_record WHERE version=? → 写 lock_log
4. 释放时：lock_status 已是已释放 → 直接返回成功
```

---

## 十六、API 清单速查

### 16.1 对外 API（经 Gateway）

| Method | URL | 服务 | 鉴权 |
|--------|-----|------|------|
| POST | /api/auth/register | User | 公开 |
| POST | /api/auth/login | User | 公开 |
| POST | /api/auth/logout | User | JWT |
| GET | /api/users/me | User | JWT |
| PUT | /api/users/me | User | JWT |
| PUT | /api/users/me/contact | User | JWT |
| POST | /api/users/me/certification | User | JWT |
| GET | /api/wishlist | User | JWT |
| POST | /api/wishlist | User | JWT |
| DELETE | /api/wishlist/{itemId} | User | JWT |
| POST | /api/wishlist/{itemId}/move-to-cart | User | JWT |
| GET | /api/points/me | User | JWT |
| GET | /api/points/me/ledger | User | JWT |
| GET | /api/categories | Item | 公开 |
| GET | /api/categories/{id} | Item | 公开 |
| POST | /api/categories | Item | ADMIN |
| PUT | /api/categories/{id} | Item | ADMIN |
| DELETE | /api/categories/{id} | Item | ADMIN |
| GET | /api/items | Item | 公开 |
| GET | /api/items/{id} | Item | 公开 |
| POST | /api/items | Item | JWT |
| PUT | /api/items/{id} | Item | JWT |
| DELETE | /api/items/{id} | Item | JWT |
| PUT | /api/items/{id}/publish | Item | JWT |
| PUT | /api/items/{id}/off-shelf | Item | JWT |
| GET | /api/items/mine | Item | JWT |
| GET | /api/cart | Trade | JWT |
| POST | /api/cart/items | Trade | JWT |
| PUT | /api/cart/items/{itemId} | Trade | JWT |
| DELETE | /api/cart/items/{itemId} | Trade | JWT |
| DELETE | /api/cart | Trade | JWT |
| GET | /api/cart/settle-preview | Trade | JWT |
| POST | /api/trades/checkout | Trade | JWT + 幂等 |
| POST | /api/trades/direct | Trade | JWT + 幂等 |
| GET | /api/trades/buyer | Trade | JWT |
| GET | /api/trades/seller | Trade | JWT |
| GET | /api/trades/{tradeNo} | Trade | JWT |
| PUT | /api/trades/{tradeNo}/confirm | Trade | JWT |
| PUT | /api/trades/{tradeNo}/complete | Trade | JWT |
| PUT | /api/trades/{tradeNo}/cancel | Trade | JWT |
| GET | /api/notifications | Trade | JWT |
| PUT | /api/notifications/{id}/read | Trade | JWT |
| PUT | /api/notifications/read-all | Trade | JWT |
| POST | /api/ai/describe | AI | JWT |
| POST | /api/ai/search | AI | JWT |

**合计：对外 API 38 个**

### 16.2 内部 Feign API

**合计：对外 API 38 个 · 内部 Feign API 11 个**

---

## 十七、后续文档

| 文档 | 状态 |
|------|------|
| PRD v0.3 | ✅ |
| 系统总体架构 v1.0 | ✅ |
| 数据库设计 v1.0 | ✅ |
| REST API 规范 v1.1 | ✅ 本文档 |
| DDL 脚本（`sql/`） | ✅ 已落地 |

---

> **文档结束 · v1.1**  
> API 已在 Day 1–9 实现。下一步：Apifox 集合与故障测试脚本（Day 10–13）。
