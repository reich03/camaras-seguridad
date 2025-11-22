package com.security.camera.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;


@Configuration
public class ThreadPoolConfig {

    @Value("${thread.pool.core-size:10}")
    private int corePoolSize;

    @Value("${thread.pool.max-size:20}")
    private int maxPoolSize;

    @Value("${thread.pool.queue-capacity:100}")
    private int queueCapacity;

    
    @Bean(name = "videoProcessingExecutor")
    public Executor videoProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("VideoProcessor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        System.out.println("===========================================");
        System.out.println("Thread Pool Configuration (Object Pool Pattern):");
        System.out.println("Core Pool Size: " + corePoolSize);
        System.out.println("Max Pool Size: " + maxPoolSize);
        System.out.println("Queue Capacity: " + queueCapacity);
        System.out.println("===========================================");
        
        return executor;
    }
}
