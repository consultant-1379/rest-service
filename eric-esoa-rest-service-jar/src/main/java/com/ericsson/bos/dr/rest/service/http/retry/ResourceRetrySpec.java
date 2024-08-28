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
package com.ericsson.bos.dr.rest.service.http.retry;

import static com.ericsson.bos.dr.rest.service.utils.ExceptionChecks.isConnectionTimeoutOrRefused;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.service.utils.SpringContextHolder;
import com.ericsson.bos.dr.rest.web.v1.api.model.RetryHandlerDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.RetryPolicyDto;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Dynamic retry based on conditions and policies defined in list of <code>RetryHandlerDto</code>.
 * When first called, the condition in each <code>RetryHandlerDto</code> is tested to find a matching handler.
 * If a match is found, then retry is performed according to the policy in the <code>RetryHandlerDto</code>.
 * If no match is found, then no retry is performed.
 * <p>
 * The condition is required to be a valid jinja expression. The substitution context supplied to the expression consists
 * of the response headers (response.headers), response code (response.code) and response body (response.body). In addition
 * a substitution property 'connectionTimeout' with value true/false is included to support retrying when connection times out.
 * </p>
 */
public class ResourceRetrySpec extends Retry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRetrySpec.class);

    private final List<RetryHandlerDto> retryHandlers;
    private RetryHandlerDto matchedRetryHandler;

    /**
     * ResourceRetrySpec.
     *
     * @param retryHandlers      retry handlers
     */
    public ResourceRetrySpec(final List<RetryHandlerDto> retryHandlers) {
        this.retryHandlers = retryHandlers;
    }

    @Override
    public Publisher<?> generateCompanion(final Flux<Retry.RetrySignal> retrySignals) {
        return retrySignals.flatMap(this::processRetrySignal);
    }

    private Mono<Long> processRetrySignal(final Retry.RetrySignal rs) {
        final var failure = rs.failure();
        final var retriesAttempted = rs.totalRetries();
        if (retriesAttempted == 0 && isRetryable(failure)) {
            // attempt to find retryHandler after first failure
            this.matchedRetryHandler = retryHandlers.stream()
                    .filter(rh -> testCondition(rh, (WebClientException) failure)).findFirst()
                    .orElseThrow(() -> Exceptions.propagate(failure));
        }

        if (this.matchedRetryHandler != null) {
            final RetryPolicyDto retryPolicy = this.matchedRetryHandler.getRetryPolicy();
            if (retriesAttempted < retryPolicy.getMaxRetries()) {
                final var duration = Duration.parse("PT".concat(retryPolicy.getBackOffSeconds().toString()).concat("S"));
                return Mono.delay(duration).thenReturn(rs.totalRetries());
            }
        }

        throw Exceptions.propagate(failure);
    }

    private boolean isRetryable(final Throwable failure) {
        return failure instanceof WebClientResponseException
                || (failure instanceof final WebClientRequestException requestException
                        && isConnectionTimeoutOrRefused(requestException));
    }

    private boolean testCondition(final RetryHandlerDto retryHandler, final WebClientException webClientException) {
        try {
            final Map<String, Object> substitutionCtx = new RetrySubstitutionCtx(webClientException).get();
            final SubstitutionEngine substitutionEngine = SpringContextHolder.getBean(SubstitutionEngine.class);
            final boolean result = Boolean.parseBoolean(substitutionEngine.render(retryHandler.getRetryCondition(), substitutionCtx));
            if (result) {
                LOGGER.info("Found matching retry handler: {}", retryHandler);
            }
            return result;
        } catch (final Exception e) {
            LOGGER.error("Error processing retry handler condition: " + retryHandler.getRetryCondition(), e);
        }
        return false;
    }
}