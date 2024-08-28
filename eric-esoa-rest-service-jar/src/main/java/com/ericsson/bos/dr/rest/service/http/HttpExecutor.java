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
package com.ericsson.bos.dr.rest.service.http;

import static com.ericsson.bos.dr.rest.service.utils.ExceptionChecks.isConnectionIssue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ericsson.bos.dr.rest.service.http.retry.ResourceRetrySpec;
import com.ericsson.bos.dr.rest.service.utils.URIEncoder;
import com.ericsson.bos.dr.rest.web.v1.api.model.RetryHandlerDto;
import com.ericsson.bos.so.common.logging.security.SecurityLogger;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

/**
 * Executes HTTP requests based on the action properties from the application configuration.
 * Uses the Spring Fwk WebClient in synchronous mode for now, i.e. blocking.
 * The class is abstract and delegates configuration of the SslContext to extending classes.
 */
public abstract class HttpExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpExecutor.class);

    //remove in https://eteamproject.internal.ericsson.com/browse/ESOA-12900
    @Value("${spring.codec.max-in-memory-size-kb}")
    private String webClientInMemorySize;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ConnectionProviderProperties connectionProviderProperties;

    private ConnectionProvider connectionProvider;

    /**
     * Initialize Connection Provider.
     */
    @PostConstruct
    void init() {
        connectionProvider = ConnectionProvider.builder("rest-service")
                .maxConnections(connectionProviderProperties.getMaxConnections())
                .maxIdleTime(connectionProviderProperties.getMaxIdleTime())
                .maxLifeTime(connectionProviderProperties.getMaxLifeTime())
                .evictInBackground(connectionProviderProperties.getEvictInterval())
                .disposeInactivePoolsInBackground(connectionProviderProperties.getDisposeInterval(),
                        connectionProviderProperties.getPoolInactivityTime())
                .build();
    }

    /**
     * Executed http request using the <code>WebClient</code>.
     *
     * @param properties the http properties
     * @return ResponseEntity
     */
    public ResponseEntity<byte[]> execute(final HttpRequest properties) {
        return execute(properties, Collections.emptyList());
    }

    /**
     * Executed http request using the <code>WebClient</code> configured to retry
     * failed request when matching <code>RetryHandlerDto</code>  is found.
     *
     * @param properties    the http properties
     * @param retryHandlers retry handlers
     * @return ResponseEntity
     */
    public ResponseEntity<byte[]> execute(final HttpRequest properties, final List<RetryHandlerDto> retryHandlers) {
        final ClientHttpConnector connector = new ReactorClientHttpConnector(configureHttpClient(properties));
        final var webClient = webClientBuilder.clone()
            .clientConnector(connector)
            .exchangeStrategies(ExchangeStrategies                  //remove in https://eteamproject.internal.ericsson.com/browse/ESOA-12900
                .builder()
                .codecs(codecs -> codecs
                    .defaultCodecs()
                    .maxInMemorySize(Integer.parseInt(webClientInMemorySize) * 1024))
                .build())
            .build();

        final URI uri = Boolean.TRUE.equals(properties.getEncodeUrl()) ?
            URIEncoder.fromString(properties.getUrl()) : URI.create(properties.getUrl());
        final WebClient.RequestBodySpec requestSpec = webClient.method(Objects.requireNonNull(HttpMethod.valueOf(properties.getMethod())))
            .uri(uri)
            .headers(httpHeaders -> httpHeaders.addAll(properties.getHeaders()));
        properties.getBody().ifPresent(requestSpec::bodyValue);

        LOGGER.debug("Execute http request: {}:{}", properties.getMethod(), uri);
        final ResponseEntity<byte[]> response = requestSpec.retrieve()
            .toEntity(byte[].class)
            .retryWhen(new ResourceRetrySpec(retryHandlers))
            .onErrorResume(error -> {
                if (isConnectionIssue(error)) {
                    SecurityLogger.withFacility(
                        () -> LOGGER.error("Lost connectivity: {}. Properties: [{}]", error.getMessage()
                            .trim(), properties)
                    );
                }
                if (error instanceof final WebClientResponseException responseException) {
                    LOGGER.error("WebClient response error", responseException);
                    return Mono.just(ResponseEntity
                        .status(responseException.getStatusCode())
                        .headers(responseException.getHeaders())
                        .body(responseException.getResponseBodyAsByteArray()));
                }
                return Mono.error(error);
            }).block();

        Optional.ofNullable(response).ifPresent(r -> {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Http Response: code={}, headers={}", response.getStatusCode(), response.getHeaders());
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Http Response body: {}", response.getBody());
            }
        });

        return response;
    }

    private HttpClient configureHttpClient(final HttpRequest httpRequest) {
        var httpClient = HttpClient.create(connectionProvider)
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        final var connectTimeoutSeconds = httpRequest.getConnectTimeoutSeconds();
        final var writeTimeoutSeconds = httpRequest.getWriteTimeoutSeconds();
        final var readTimeoutSeconds = httpRequest.getReadTimeoutSeconds();

        if (connectTimeoutSeconds != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Configuring connection timeout: {}", connectTimeoutSeconds);
            }
            httpClient = httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(connectTimeoutSeconds));
        }
        if (writeTimeoutSeconds != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Configuring write timeout: {}", writeTimeoutSeconds);
            }
            httpClient = httpClient.doOnConnected(
                connection -> connection.addHandlerLast(new WriteTimeoutHandler(writeTimeoutSeconds)));
        }
        if (readTimeoutSeconds != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Configuring read timeout: {}", readTimeoutSeconds);
            }
            httpClient = httpClient.doOnConnected(
                connection -> connection.addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds)));
        }

        // Note: netty will only re-use a connection pool if both sslContext and connectionProvider instances are the same.
        // For example if new sslContext is created for every request, then a new connection pool is created for each request. There will
        // be no re-use of connections and will result in pools, each with a single connection that is not released.
        final SslContext sslContext = configureSslContext(httpRequest);
        httpClient = httpClient.secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        return httpClient;
    }

    /**
     * Extending classes should implement this method to return the SslContext for secure communication.
     *
     * @param httpRequest http request properties
     * @return SslContext
     */
    protected abstract SslContext configureSslContext(HttpRequest httpRequest);
}
