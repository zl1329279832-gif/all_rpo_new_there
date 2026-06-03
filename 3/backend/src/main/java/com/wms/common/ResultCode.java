package com.wms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    SYSTEM_ERROR(1000, "系统错误"),
    DATABASE_ERROR(1001, "数据库操作错误"),
    REDIS_ERROR(1002, "Redis操作错误"),

    LOGIN_ERROR(2000, "登录失败"),
    USER_NOT_EXIST(2001, "用户不存在"),
    PASSWORD_ERROR(2002, "密码错误"),
    USER_DISABLED(2003, "用户已被禁用"),
    TOKEN_INVALID(2004, "Token无效"),
    TOKEN_EXPIRED(2005, "Token已过期"),

    DATA_NOT_EXIST(3000, "数据不存在"),
    DATA_ALREADY_EXIST(3001, "数据已存在"),
    DATA_STATUS_ERROR(3002, "数据状态错误"),
    DATA_CANNOT_DELETE(3003, "数据无法删除"),

    INVENTORY_SHORTAGE(4000, "库存不足"),
    INVENTORY_LOCKED(4001, "库存已锁定"),
    INVENTORY_FROZEN(4002, "库存已冻结"),
    INVENTORY_EXPIRED(4003, "库存已过期"),
    LOCATION_CAPACITY_NOT_ENOUGH(4004, "库位容量不足"),
    BATCH_NOT_EXIST(4005, "批次不存在"),
    REPEAT_PICKING(4006, "重复拣货"),
    OUTBOUND_CANCELLED(4007, "出库单已取消"),
    ALLOCATE_FAILED(4008, "库存分配失败"),
    REDUCE_FAILED(4009, "库存扣减失败"),

    LOCK_ACQUIRE_FAILED(5000, "获取分布式锁失败"),
    LOCK_RELEASE_FAILED(5001, "释放分布式锁失败"),
    LOCK_TIMEOUT(5002, "锁超时"),

    VALIDATE_FAILED(6000, "校验失败"),
    ILLEGAL_OPERATION(6001, "非法操作"),
    BUSINESS_STATUS_ERROR(6002, "业务状态错误");

    private final Integer code;
    private final String message;
}
