package com.github.gelald.schedule.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@Data
@Component
@ConfigurationProperties(prefix = "control")
public class ControlProperties {
    private boolean cronTaskEnabled = true;
    private boolean fixedDelayTaskEnabled = true;
}
