/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.bos.dr.rest.service.cache;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ericsson.bos.dr.rest.service.auth.TokenData;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

/**
 * Cache Configuration
 * Contains a bean for each <code>CaffeineCache</code> used in the application
 */
@Configuration
public class CacheConfiguration {

    @Value("${service.connected-system.cache.accessExpiry}")
    private long subsystemCacheAccessExpiry;

    /**
     * auth_token_cache bean
     * @return CaffeineCache bean
     */
    @Bean
    @Primary
    public CaffeineCache authTokenCache() {
        return new CaffeineCache("auth_token_cache",
            Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(1000)
                .expireAfter(
                    new Expiry<Object, Object>() {
                        @Override
                        public long expireAfterCreate(
                            @NonNull Object key, @NonNull Object tokenData, long currentTime) {

                            if (Objects.nonNull(((TokenData)tokenData).getExpireSeconds())) {
                                return TimeUnit.SECONDS.toNanos(((TokenData)tokenData).getExpireSeconds());
                            }
                            return 0;
                        }

                        @Override
                        public long expireAfterUpdate(
                            @NonNull Object key,
                            @NonNull Object tokenData,
                            long currentTime,
                            @NonNegative long currentDuration) {
                            return currentDuration;
                        }

                        @Override
                        public long expireAfterRead(
                            @NonNull Object key,
                            @NonNull Object tokenData,
                            long currentTime,
                            @NonNegative long currentDuration) {
                            return currentDuration;
                        }
                    })
                .build());
    }

    /**
     * subsystem_cache bean
     * @return CaffeineCache bean
     */
    @Bean
    public CaffeineCache subsystemCache() {
        return new CaffeineCache("subsystem_cache",
            Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(1000)
                .expireAfterAccess(subsystemCacheAccessExpiry, TimeUnit.SECONDS)
                .build());
    }

}
