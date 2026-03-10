package com.lpw.joyfoodmall.common;

import lombok.Data;

/**
 * 状态码规范：
 * 200 - 操作成功
 * 400 - 参数错误/业务逻辑错误（如不能删除自己）
 * 401 - 未认证（未登录/Token失效）
 * 403 - 无权限
 * 404 - 资源不存在
 * 500 - 服务器内部错误
 */
@Data
public class Result<T> {
    // 响应状态
    private boolean success;
    // 状态码
    private int code;
    // 提示消息
    private String message;
    // 响应数据
    private T data;

    private Result(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ====================== 成功响应 ======================
    // 成功（带数据+自定义消息+自定义状态码）
    public static <T> Result<T> success(T data, int code, String message) {
        return new Result<>(true, code, message, data);
    }

    // 成功（带数据+默认成功消息+默认200码）
    public static <T> Result<T> success(T data) {
        return new Result<>(true, 200, "操作成功", data);
    }

    // 成功（带数据+自定义消息+默认200码）
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, 200, message, data);
    }

    // 成功（无数据+自定义消息+默认200码）
    public static <T> Result<T> message(String message) {
        return new Result<>(true, 200, message, null);
    }

    // 成功（无数据+默认消息+默认200码）
    public static <T> Result<T> success() {
        return new Result<>(true, 200, "操作成功", null);
    }

    // ====================== 失败响应 ======================
    // 失败（自定义消息+自定义状态码+无数据）
    public static <T> Result<T> error(String message, int code) {
        return new Result<>(false, code, message, null);
    }

    // 失败（自定义消息+默认500码+无数据）
    public static <T> Result<T> error(String message) {
        return new Result<>(false, 500, message, null);
    }

    // 失败（自定义消息+自定义状态码+带数据）
    public static <T> Result<T> error(String message, int code, T data) {
        return new Result<>(false, code, message, data);
    }
}