package cn.ynu.campus.relife.common.core.exception;

/**
 * 全局错误码枚举骨架，完整清单见 docs/API接口规范.md §3.2
 */
public enum ErrorCode {

    SUCCESS(0, "success"),

    PARAM_INVALID(40001, "参数校验失败"),
    PAGE_INVALID(40002, "分页参数非法"),

    UNAUTHORIZED(40101, "未登录"),
    TOKEN_INVALID(40102, "Token 无效"),
    TOKEN_EXPIRED(40103, "Token 已过期"),
    TOKEN_REVOKED(40104, "Token 已失效"),

    FORBIDDEN(40301, "无权限访问"),
    FORBIDDEN_NOT_OWNER(40302, "无权操作他人资源"),

    NOT_FOUND(40401, "资源不存在"),

    DUPLICATE_REQUEST(40901, "重复请求"),
    RESOURCE_EXISTS(40902, "资源已存在"),

    CONTACT_REQUIRED(42201, "联系方式未设置"),
    CART_EMPTY(42202, "购物车为空"),
    ITEM_NOT_ON_SALE(42203, "物品非在售状态"),
    STOCK_INSUFFICIENT(42204, "库存不足"),
    TRADE_STATUS_INVALID(42205, "交易状态不允许此操作"),
    CANNOT_BUY_OWN_ITEM(42206, "不能购买自己的物品"),

    RATE_LIMITED(42900, "请求过于频繁"),

    INTERNAL_ERROR(50000, "系统内部错误"),
    DOWNSTREAM_UNAVAILABLE(50001, "下游服务不可用"),

    USER_LOGIN_FAILED(10001, "用户名或密码错误"),
    USER_LOCKED(10002, "账号已锁定"),
    LOGIN_NAME_EXISTS(10003, "登录名已存在"),
    CAMPUS_ID_EXISTS(10004, "学号已注册"),

    ITEM_NOT_FOUND(20001, "物品不存在"),
    ITEM_OFF_SALE(20002, "物品已下架或已售出"),
    ITEM_NOT_OWNER(20003, "非物品发布者"),

    TRADE_NOT_FOUND(30001, "交易单不存在"),
    TRADE_STATUS_ERROR(30002, "交易状态不允许操作"),

    STOCK_LOCK_FAILED(41001, "库存锁定失败"),
    STOCK_RECORD_NOT_FOUND(41002, "库存记录不存在"),

    AI_DEGRADED(50002, "AI 已降级返回模板");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
