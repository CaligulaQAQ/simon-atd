package com.simon.common.util;

import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author yangbin.syb
 */
@Slf4j
public class RetryUtils {
    private static final long DELAY = 1000L;

    public static <V> V retry(Callable<V> callable, int retry, String message) {
        return retryLogic(callable, retry, message);
    }

    public static void retry(RunnableWithException runnable, int retry, String message) {
        retryLogic(() -> {
            runnable.run();
            return null;
        }, retry, message);
    }

    private static <T> T retryLogic(Callable<T> callable, int retry, String message) {
        int counter = 0;
        Exception lastException = null;
        while (counter < retry) {
            try {
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                counter++;
                log.info("retry {} / {}, {}", counter, retry, e.getClass().toString());

                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (StringUtils.isNotBlank(message)) {
            log.info(message);
        }
        if (null != lastException) {
            log.info(ExceptionUtils.getStackTrace(lastException));
        }
        throw new RuntimeException(message);
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}