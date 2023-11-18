package com.simon.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yzy
 */
@Component
@Slf4j
public class RequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        Map<String, String> map = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(paramName);
            if (parameterValues.length == 1) {
                if (parameterValues[0].length() != 0) {
                    map.put(paramName, parameterValues[0]);
                }
            }
        }
        log.info("request path = {},param = {}", request.getRequestURI(), JSON.toJSONString(map));
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
