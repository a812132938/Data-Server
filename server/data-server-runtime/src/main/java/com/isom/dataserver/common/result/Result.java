package com.isom.dataserver.common.result;

import com.isom.dataserver.common.exception.ErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.slf4j.MDC;

@Data
@ApiModel(description = "统一响应结果")
public class Result<T> {
    @ApiModelProperty(value = "状态码，0表示成功", example = "0")
    private int code;
    @ApiModelProperty(value = "响应消息", example = "ok")
    private String message;
    @ApiModelProperty(value = "响应数据")
    private T data;
    @ApiModelProperty(value = "链路追踪ID")
    private String traceId;

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.SUCCESS);
        r.setMessage(ResultCode.SUCCESS_MSG);
        r.setTraceId(MDC.get("traceId"));
        return r;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = ok();
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setTraceId(MDC.get("traceId"));
        return r;
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }
}
