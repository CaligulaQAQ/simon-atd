package com.simon.common.result;

import java.io.Serializable;

import com.simon.common.constant.Constant.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.MDC;

/**
 * <p> 返回结果封装 </p>
 *
 * @date 2022/5/16 13:56
 */

@Data
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Result implements Serializable {

    private Integer code;
    private String  message;
    private String  errors;
    private String  requestId;
    private Object  data;
    private Boolean success;

    public Result() {
        this.success = true;
        this.requestId = MDC.get(BaseParam.REQUEST_ID);
        this.code = 0;
    }

    public Result(Object data) {
        this.success = true;
        this.code = 0;
        this.data = data;
    }

    public static Result from(Boolean success,
        Integer code,
        Object data,
        String message,
        String errors,
        String requestId) {
        Result result = new Result();
        result.success = success;
        result.code = code;
        result.data = data;
        result.message = message;
        result.errors = errors;
        result.requestId = requestId;
        return result;
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.success = true;
        result.code = 0;
        result.data = data;
        return result;
    }

    public static Result fail() {
        Result result = new Result();
        result.success = false;
        result.code = -1;
        result.message = "server_error_unknown";
        return result;
    }

    public static Result fail(String message) {
        Result result = new Result();
        result.success = false;
        result.code = -1;
        result.message = message;
        return result;
    }

    public static Result fail(Integer code) {
        Result result = new Result();
        result.success = false;
        result.code = code;
        return result;
    }

    public static Result fail(String message, Integer code) {
        Result result = new Result();
        result.success = false;
        result.message = message;
        result.code = code;
        return result;
    }

    public static Result fail(String message, Integer code, Object data) {
        Result result = new Result();
        result.success = false;
        result.message = message;
        result.data = data;
        result.code = code;
        return result;
    }

    public static Result isSuccess(boolean isSuccess) {
        Result result = new Result();
        result.success = isSuccess;
        result.code = isSuccess ? 0 : -1;
        result.message = isSuccess ? "" : "server_error_unknown";
        return result;
    }

}
