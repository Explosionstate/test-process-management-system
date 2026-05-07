package com.example.tracker.common;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiResponse<Void> business(BusinessException e) {
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiResponse<Void> security(SecurityException e) {
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiResponse<Void> databaseConnection(CannotGetJdbcConnectionException e) {
        return ApiResponse.error("无法连接本地 MySQL，请确认 MySQL 已启动，并检查 application.yml 中的账号密码");
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiResponse<Void> databaseSchema(BadSqlGrammarException e) {
        return ApiResponse.error("数据库表不存在或结构不匹配，请先执行 src/main/resources/db/schema.sql 和 data.sql");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiResponse<Void> error(Exception e) {
        return ApiResponse.error("系统异常：" + e.getMessage());
    }
}
