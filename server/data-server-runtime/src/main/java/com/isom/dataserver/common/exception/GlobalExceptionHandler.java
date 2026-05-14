package com.isom.dataserver.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.isom.dataserver.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLogin(NotLoginException e) {
        return Result.fail(ErrorCode.AUTH_FAILED.getCode(), "未登录或 token 已失效");
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBiz(BizException e) {
        if (e.getErrorCode().getHttpStatus() >= 500) {
            log.warn("Business exception: httpStatus={}, code={}, message={}",
                    e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getMessage());
        } else {
            log.info("Business exception: httpStatus={}, code={}, message={}",
                    e.getErrorCode().getHttpStatus(), e.getErrorCode().getCode(), e.getMessage());
        }
        Result<Void> result = Result.fail(e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("参数校验失败");
        return Result.fail(40003, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
