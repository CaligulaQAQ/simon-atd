package com.simon.web.interceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.simon.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p> 统一异常拦截 </p>
 *
 * <p> 该拦截器统一在 Controller 层处理后端异常／错误，包装成HTTP响应返回给前端。
 * 业务层层无需自己包装HTTP响应，只需简单向上抛异常（e.g. RuntimeException），该拦截器会统一处理。 </p>
 */
@Slf4j
@ControllerAdvice(basePackages = {"com.simon"})
public class WebExceptionInterceptor {

    /**
     * 统一处理未被捕获的异常
     *
     * @param exception 异常
     * @return Result响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(HttpServletRequest request, Exception exception) {
        Result result = new Result();
        result.setCode(-1);
        result.setSuccess(false);
        result.setData(null);
        String message = Optional.ofNullable(exception.getMessage()).orElse("server_error_unknown");
        result.setMessage(message);

        String requestBaseMsg = String.format("productionMode = [false], method = [%s], uri = [%s]",
                request.getMethod(), request.getRequestURI());
        log.error(requestBaseMsg, exception);
        return result;
    }


    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result handleValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            message = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(","));
        }
        String requestBaseMsg = String.format("productionMode = [false], method = [%s], uri = [%s]",
                request.getMethod(), request.getRequestURI());
        log.error(requestBaseMsg, e);
        Result result = new Result();
        result.setCode(-1);
        result.setSuccess(false);
        result.setData(null);
        result.setMessage(message);
        return result;
    }
}
