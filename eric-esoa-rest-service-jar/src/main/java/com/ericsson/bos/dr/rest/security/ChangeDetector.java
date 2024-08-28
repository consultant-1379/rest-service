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
package com.ericsson.bos.dr.rest.security;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.kubernetes.commons.config.reload.ConfigReloadProperties;
import org.springframework.core.env.AbstractEnvironment;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * To handle the kubernetes connection that is being made while the server is running.
 * Prevent the case that when doing a kubernetes upgrade the api server can close the HTTPS socket connection but not inform the application.
 */
public abstract class ChangeDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeDetector.class);

    protected AbstractEnvironment environment;
    protected KubernetesClient kubernetesClient;
    protected ConfigReloadProperties properties;

    private final AtomicBoolean resubscribePending = new AtomicBoolean(false);
    private final AtomicInteger currentReconnectAttempt = new AtomicInteger(0);
    private final ScheduledExecutorService executor;
    private final long initialTimeout;
    private final int maxTimeoutPower;

    /**
     * Constructor
     * @param environment AbstractEnvironment
     * @param properties ConfigReloadProperties
     * @param kubernetesClient kubernetesClient
     */
    protected ChangeDetector(final AbstractEnvironment environment,
            final KubernetesClient kubernetesClient,
            final ConfigReloadProperties properties
    ) {
        this(environment, kubernetesClient, properties, 8L, 4);
    }

    /**
     * Constructor
     * A new custom thread is created for the scheduled executor
     * @param environment AbstractEnvironment
     * @param properties ConfigReloadProperties
     * @param kubernetesClient kubernetesClient
     * @param initialTimeout initial timeout
     * @param maxTimeoutPower max timout power
     */
    protected ChangeDetector(final AbstractEnvironment environment,
                          final KubernetesClient kubernetesClient,
                          final ConfigReloadProperties properties,
                          final long initialTimeout,
                          final int maxTimeoutPower) {

        this.environment = environment;
        this.kubernetesClient = kubernetesClient;
        this.properties = properties;
        this.initialTimeout = initialTimeout;
        this.maxTimeoutPower = maxTimeoutPower;
        this.executor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "Executor for re-subscribe " + System.identityHashCode(ChangeDetector.this))
        );
    }

    /**
     * Post Construct for subscribe method
     */
    @PostConstruct
    public abstract void subscribe();

    /**
     * Shuts down the executor
     */
    @PreDestroy
    public final void shutdown() {
        LOGGER.info("Shutdown in {}", this);
        if (!executor.isShutdown()) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    LOGGER.warn("Executor didn't terminate in time after shutdown, killing it in: {}", this);
                    executor.shutdownNow();
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (final KubernetesClientException t) {
                throw KubernetesClientException.launderThrowable(t);
            }
        }
    }

    /**
     * Schedule re-subscribe task to enable the certificate detector
     */
    public final void scheduleResubscribe() {
        LOGGER.info("Submitting re-subscribe task to the executor");
        executor.submit(() -> {
            if (!resubscribePending.compareAndSet(false, true)) {
                LOGGER.info("Re-subscribe already scheduled");
                return;
            }
            LOGGER.info("Scheduling task for re-subscribe attempt");
            executor.schedule(() -> {
                try {
                    LOGGER.info("Re-subscribe attempt started");
                    subscribe();
                    resubscribePending.set(false);
                } catch (final Exception e) {
                    LOGGER.error("Unexpected error in re-subscribe attempt");
                    shutdown();
                }
            }, nextResubscribeInterval(), TimeUnit.SECONDS);
        });
    }

    private long nextResubscribeInterval() {
        int powerOfTwo = currentReconnectAttempt.getAndIncrement();
        if (powerOfTwo > maxTimeoutPower) {
            powerOfTwo = maxTimeoutPower;
        }
        final long resubscribeInterval = initialTimeout << powerOfTwo;
        LOGGER.info("Current reconnect backoff is {} seconds (T{})", resubscribeInterval, powerOfTwo);
        return resubscribeInterval;
    }

    /**
     * An abstract class for a subscriber that implements the Watcher
     *
     * @param <E> Kubernetes resource type for whose events the Subscriber should watch - e.g. {@link Secret}}
     */
    abstract class Subscriber<E> implements Watcher<E> {

        private final String name;

        /**
         * Constructor
         * @param detectorName detector name
         */
        Subscriber(final String detectorName) {
            this.name = detectorName;
        }

        /**
         * Called when the watcher is closed, indicating that the subscriber is disabled.
         * If the executor is not shutdown, it schedules a resubscribe operation.
         */
        @Override
        public final void onClose(final WatcherException cause) {
            LOGGER.warn("{} is DISABLED", name);
            if (!executor.isShutdown()) {
                scheduleResubscribe();
            }
        }
    }
}
