package com.example.bank.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 4;

    private static final int MAX_POOL_SIZE = 8;

    private static final int QUEUE_CAPACITY = 100;

    private static final int AWAIT_TERMINATION_SECONDS = 30;

    private static final String THREAD_NAME_PREFIX =
            "bank-async-";

    @Bean(name = "bankTaskExecutor")
    public AsyncTaskExecutor bankTaskExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(CORE_POOL_SIZE);

        executor.setMaxPoolSize(MAX_POOL_SIZE);

        executor.setQueueCapacity(QUEUE_CAPACITY);

        executor.setThreadNamePrefix(
                THREAD_NAME_PREFIX
        );

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.setWaitForTasksToCompleteOnShutdown(
                true
        );

        executor.setAwaitTerminationSeconds(
                AWAIT_TERMINATION_SECONDS
        );

        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {

        return new AsyncUncaughtExceptionHandler() {

            @Override
            public void handleUncaughtException(
                    Throwable throwable,
                    Method method,
                    Object... params) {

                log.error(
                        "Unhandled exception in asynchronous method: {}",
                        method.getName(),
                        throwable
                );
            }
        };
    }
}