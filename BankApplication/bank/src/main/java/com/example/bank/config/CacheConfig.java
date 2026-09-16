package com.example.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(
            @Value(
                    "${spring.cache.cache-names:userById,userByUsername,customerById,customerByUserId,employeeById,employeeByUserId,accountById,accountByNumber,loanById,investmentById}"
            )
            String cacheNames,

            @Value(
                    "${spring.cache.caffeine.spec:maximumSize=1000,expireAfterWrite=10m}"
            )
            String cacheSpecification) {

        CaffeineCacheManager caffeineCacheManager =
                new CaffeineCacheManager();

        caffeineCacheManager.setCacheNames(
                List.of(cacheNames.split(","))
        );

        caffeineCacheManager.setCacheSpecification(
                cacheSpecification
        );

        return new TransactionAwareCacheManagerProxy(
                caffeineCacheManager
        );
    }
}