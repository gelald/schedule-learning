package com.github.gelald.schedule.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@Slf4j
@Component
public class SimpleTask {

    @Scheduled(cron = "0/30 * * * * *")
    public void simpleTask() {
        log.info("SimpleTask scheduled");
    }

    @Scheduled(cron = "#{@simpleProperties.cron}")
    public void simpleTask2() {
        log.info("SimpleTask2 scheduled");
    }
}
