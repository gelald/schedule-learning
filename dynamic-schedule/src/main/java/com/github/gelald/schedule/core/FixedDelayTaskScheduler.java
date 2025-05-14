package com.github.gelald.schedule.core;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.github.gelald.schedule.properties.TaskProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@Slf4j
@Setter
@Getter
@Component
@ConditionalOnProperty(prefix = "control", value = "fixed-delay-task-enabled", havingValue = "true", matchIfMissing = true)
public class FixedDelayTaskScheduler extends BaseTaskScheduler {
    private final TaskProperties taskProperties;
    private Pair<String, ScheduledFuture<?>> currentTask;

    public FixedDelayTaskScheduler(TaskProperties taskProperties, NacosConfigManager nacosConfigManager) {
        super(nacosConfigManager);
        this.taskProperties = taskProperties;
    }

    @Override
    protected void createTask(String mechanism) {
        // create the timer task
        FixedDelayTask task = new FixedDelayTask(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                log.info("[FixedDelayTaskScheduler] starts, {}", DATE_TIME_FORMATTER.format(now));
                int randomTime = RandomUtil.getRandom().nextInt(1000, 2000);
                log.info("[FixedDelayTaskScheduler] sleeps {}ms", randomTime);
                TimeUnit.MILLISECONDS.sleep(randomTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                LocalDateTime now = LocalDateTime.now();
                log.info("[FixedDelayTaskScheduler] finishes, {}", DATE_TIME_FORMATTER.format(now));
                log.info("");
            }
        }, Duration.ofSeconds(Integer.parseInt(mechanism)), Duration.ZERO);
        // get scheduler
        TaskScheduler scheduledTaskRegistrar;
        if (taskRegistrarHolder.getScheduler() != null) {
            scheduledTaskRegistrar = taskRegistrarHolder.getScheduler();
        } else {
            scheduledTaskRegistrar = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
        }
        // schedule the timer task
        ScheduledFuture<?> future = scheduledTaskRegistrar.scheduleWithFixedDelay(task.getRunnable(), task.getIntervalDuration());
        // cache the timer task
        setCurrentTask(Pair.of(mechanism, future));
    }

    @Override
    protected String getMechanism() {
        String fixedDelay = taskProperties.getFixedDelay();
        return fixedDelay;
    }

    @Override
    protected boolean checkChange(ConfigChangeItem changeItem) {
        String key = changeItem.getKey();
        if (!("task-schedule.fixed-delay".equals(key) || "task-schedule.fixedDelay".equals(key))) {
            log.info("fixed-delay has not been changed, doesn't response it");
            return false;
        }
        log.info("schedule config changed, new fixed-delay: {}, now refresh the task", changeItem.getNewValue());
        return true;
    }
}
