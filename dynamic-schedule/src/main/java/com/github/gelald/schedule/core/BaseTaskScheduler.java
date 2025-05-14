package com.github.gelald.schedule.core;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

/**
 * @author ngwingbun
 * date: 2025/5/12
 */
@Slf4j
public abstract class BaseTaskScheduler implements SchedulingConfigurer {
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    protected NacosConfigManager nacosConfigManager;
    protected ScheduledTaskRegistrar taskRegistrarHolder;

    public BaseTaskScheduler(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }

    @Override
    public void configureTasks(@Nonnull ScheduledTaskRegistrar taskRegistrar) {
        // set ScheduledTaskRegistrar so that we can use it to schedule the timer task
        taskRegistrarHolder = taskRegistrar;
        String mechanism = getMechanism();
        if ("-".equals(mechanism) || StrUtil.isBlank(mechanism)) {
            log.info("task mechanism: {}, task will not execute in the first time scheduling", mechanism);
            return;
        }
        createTask(mechanism);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshConfig() throws Exception {
        // add nacos config listener
        nacosConfigManager.getConfigService().addListener("dynamic-schedule.yml", "DEFAULT_GROUP", new AbstractConfigChangeListener() {
            @Override
            public void receiveConfigChange(ConfigChangeEvent event) {
                // receive config change event
                Collection<ConfigChangeItem> changeItems = event.getChangeItems();
                for (ConfigChangeItem changeItem : changeItems) {
                    log.info("config changed item: {}", changeItem.getKey());
                    boolean result = checkChange(changeItem);
                    if (!result) {
                        continue;
                    }
                    refreshTask(changeItem.getNewValue());
                }
            }
        });
        log.info("added nacos config listener successfully, especially for cron-expression");
    }

    protected void refreshTask(String mechanism) {
        // if the value is null or "-", then the timer task will be canceled
        if ("-".equals(mechanism) || StrUtil.isBlank(mechanism)) {
            log.info("mechanism: {}, task cancelled", mechanism);
            cancelTask();
            return;
        }

        if (getCurrentTask() != null) {
            if (mechanism.equals(getCurrentTask().getKey())) {
                // task has not change
                log.info("task has not change, the mechanism is the same: {}", mechanism);
            } else {
                // for update task, cancel previous task before add new task
                cancelTask();
                createTask(mechanism);
                log.info("previous task canceled, new task scheduled, mechanism: {}", mechanism);
            }
        } else {
            // current task is null, add timer task directly
            createTask(mechanism);
            log.info("task created, mechanism: {}", mechanism);
        }
    }

    protected void cancelTask() {
        ScheduledFuture<?> future = getCurrentTask().getValue();
        if (future != null) {
            // stop previous timer task
            future.cancel(false);
        }
        // remove timer task
        clearCurrentTask();
    }

    protected void clearCurrentTask() {
        setCurrentTask(null);
    }

    protected abstract void createTask(String mechanism);

    protected abstract String getMechanism();

    protected abstract boolean checkChange(ConfigChangeItem changeItem);

    protected abstract Pair<String, ScheduledFuture<?>> getCurrentTask();

    protected abstract void setCurrentTask(Pair<String, ScheduledFuture<?>> currentTask);
}
