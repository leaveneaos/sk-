package com.rjxx.taxeasy.threadpool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by Administrator on 2017/1/4.
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${threadPool.corePoolSize:200}")
    private int corePoolSize;

    @Value("${threadPool.maxPoolSize:200}")
    private int maxPoolSize;

    @Value("${threadPool.keepAliveSeconds:99999999}")
    private int keepAliveSeconds;

    @Value("${threadPool.queueCapacity:20000}")
    private int queueCapacity;

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        return threadPoolTaskExecutor;
    }

}
