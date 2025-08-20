package com.zheng.aicommunitybackend.handle;


import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String msg = split[2] + "已存在";
            log.error(msg);
            return Result.error(msg);
        }else {
            log.error("其他错误");
            return Result.error("未知错误");
        }
    }

    /**
     * 处理请求参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验失败", ex);
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getDefaultMessage()).append("; ");
        }
        return Result.error("参数校验失败: " + errorMsg.toString());
    }

    /**
     * 处理Bean校验异常
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException ex) {
        log.error("参数绑定失败", ex);
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getDefaultMessage()).append("; ");
        }
        return Result.error("参数绑定失败: " + errorMsg.toString());
    }

    /**
     * 处理约束校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("约束校验失败", ex);
        StringBuilder errorMsg = new StringBuilder();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            errorMsg.append(violation.getMessage()).append("; ");
        }
        return Result.error("约束校验失败: " + errorMsg.toString());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统异常: " + ex.getMessage());
    }

}
