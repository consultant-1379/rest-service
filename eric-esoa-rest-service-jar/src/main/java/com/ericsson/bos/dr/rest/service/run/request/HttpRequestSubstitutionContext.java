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
package com.ericsson.bos.dr.rest.service.run.request;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;

/**
 * Substitution context for Http request substitution..
 */
public class HttpRequestSubstitutionContext implements Supplier<Map<String, Object>> {

    private static final String METHOD = "method";
    private static final String REQUEST = "request";
    private static final String INPUTS = "inputs";
    private static final String BODY = "body";

    private final HttpRequest httpRequest;
    private final RunExecutionContext runExecutionContext;

    /**
     * Http request substitution context.
     * @param httpRequest http request
     * @param runExecutionContext run execution context
     */
    public HttpRequestSubstitutionContext(final HttpRequest httpRequest, final RunExecutionContext runExecutionContext) {
        this.httpRequest = httpRequest;
        this.runExecutionContext = runExecutionContext;
    }

    @Override
    public Map<String, Object> get() {
        final Map<String, Object> substitutionCtx = new HashMap<>();
        final Map<String, Object> requestProperties = new HashMap<>();
        requestProperties.put(METHOD, runExecutionContext.getResourceMethod().getMethodName());
        httpRequest.getBody().ifPresent(b -> requestProperties.put(BODY, b));
        substitutionCtx.put(REQUEST, requestProperties);
        substitutionCtx.put(INPUTS, runExecutionContext.getRunRequest().getInputs());
        substitutionCtx.put(runExecutionContext.getSubsystem().getAuthKey(), runExecutionContext.getAuthToken());
        return substitutionCtx;
    }
}