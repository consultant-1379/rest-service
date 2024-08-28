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

import java.net.ConnectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ericsson.bos.dr.rest.service.utils.JSON;

/**
 * Substitution context for evaluation of Retry conditions.
 */
public class RetrySubstitutionCtx implements Supplier<Map<String, Object>> {

    private static final String RESPONSE_KEY = "response";
    private static final String RESPONSE_CODE_KEY = "code";
    private static final String RESPONSE_HEADERS_KEY = "headers";
    private static final String RESPONSE_BODY_KEY = "body";
    private static final String CONNECTION_TIMEOUT_KEY = "connectionTimeout";

    private final WebClientException webClientException;

    /**
     * RetrySubstitutionCtx.
     * @param webClientException webClientException
     */
    public RetrySubstitutionCtx(final WebClientException webClientException) {
        this.webClientException = webClientException;
    }

    @Override
    public Map<String, Object> get() {
        final Map<String, Object> substitutionCtx = new HashMap<>();
        substitutionCtx.put(CONNECTION_TIMEOUT_KEY, webClientException.getRootCause() instanceof ConnectException);
        final Map<String, Object> responseSubstitionCtx = new HashMap<>();
        if (webClientException instanceof final WebClientResponseException webClientResponseException) {
            responseSubstitionCtx.put(RESPONSE_HEADERS_KEY, Optional.ofNullable(webClientResponseException.getHeaders())
                    .map(HttpHeaders::toSingleValueMap).orElse(Collections.emptyMap()));
            responseSubstitionCtx.put(RESPONSE_CODE_KEY, webClientResponseException.getStatusCode().value());
            final Object body = Optional.ofNullable(webClientResponseException.getResponseBodyAsString())
                    .map(s -> JSON.isJsonStr(s) ? JSON.readObjectForSubstitution(s) : s).orElse(null);
            responseSubstitionCtx.put(RESPONSE_BODY_KEY, body);
        } else {
            responseSubstitionCtx.put(RESPONSE_HEADERS_KEY, Collections.emptyMap());
            responseSubstitionCtx.put(RESPONSE_CODE_KEY, null);
            responseSubstitionCtx.put(RESPONSE_BODY_KEY, null);
        }
        substitutionCtx.put(RESPONSE_KEY, responseSubstitionCtx);
        return substitutionCtx;
    }
}