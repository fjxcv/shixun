package com.soft.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("未捕获异常", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "服务器内部错误";
        }
        return Result.fail(msg);
    }
}
