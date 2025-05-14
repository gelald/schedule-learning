package com.github.gelald.schedule.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@Data
@Component
@ConfigurationProperties(prefix = "task-schedule")
public class TaskProperties {
    private String cronExpression = "*/5 * * * * ?";
    private String fixedDelay = "8";
}
