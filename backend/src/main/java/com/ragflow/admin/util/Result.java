package com.ragflow.admin.util;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.code = 200;
        return r;
    }

    public static <T> Result<T> err(String message) {
        Result<T> r = new Result<>();
        r.code = 400;
        r.message = message;
        return r;
    }

    public static <T> Result<T> err(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
