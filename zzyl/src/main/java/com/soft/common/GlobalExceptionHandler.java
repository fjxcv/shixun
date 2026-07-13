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
        log.error("\u672a\u6355\u83b7\u5f02\u5e38", e);
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "\u670d\u52a1\u5668\u5185\u90e8\u9519\u8bef";
        }
        return Result.fail(msg);
    }
}
