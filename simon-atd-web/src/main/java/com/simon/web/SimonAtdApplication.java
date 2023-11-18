package com.simon.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author yzy
 * @Date 2023/11/8
 */
@SpringBootApplication(scanBasePackages = {"com.simon"})
public class SimonAtdApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimonAtdApplication.class, args);
    }
}
