package com.github.gelald.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@EnableScheduling
@SpringBootApplication
public class SimpleScheduleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleScheduleApplication.class, args);
    }
}
