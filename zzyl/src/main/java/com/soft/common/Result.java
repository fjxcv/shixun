package com.soft.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    private Long total;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok(T data, Long total) {
        Result<T> r = ok(data);
        r.setTotal(total);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.setCode(400);
        r.setMsg(msg);
        return r;
    }
}
