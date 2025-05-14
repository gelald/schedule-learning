package com.github.gelald.schedule;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@Data
@Component
public class SimpleProperties {
    private String cron = "0/10 * * * * *";
}
