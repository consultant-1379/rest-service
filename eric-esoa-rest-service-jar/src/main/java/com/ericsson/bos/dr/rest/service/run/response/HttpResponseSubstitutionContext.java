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
package com.ericsson.bos.dr.rest.service.run.response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.utils.JSON;

/**
 * Available substitution context for processing of the http response.
 */
public class HttpResponseSubstitutionContext implements Supplier<Map<String, Object>> {

    private static final String BODY = "body";
    private static final String CODE = "code";
    private static final String RESPONSE = "response";
    private static final String HEADERS = "headers";
    private static final String METHOD = "method";
    private static final String REQUEST = "request";
    private static final String INPUTS = "inputs";
    private static final String URL = "url";

    private final HttpRunResponse httpRunResponse;
    private final RunExecutionContext runExecutionContext;

    /**
     * HttpResponseSubstitutionContext.
     * @param httpRunResponse http response for run request
     * @param runExecutionContext run execution context
     */
    public HttpResponseSubstitutionContext(final HttpRunResponse httpRunResponse, final RunExecutionContext runExecutionContext) {
        this.httpRunResponse = httpRunResponse;
        this.runExecutionContext = runExecutionContext;
    }

    @Override
    public Map<String, Object> get() {
        final Map<String, Object> substitutionCtx = new HashMap<>();
        substitutionCtx.put(INPUTS, runExecutionContext.getRunRequest().getInputs());
        substitutionCtx.put(runExecutionContext.getSubsystem().getConnection().getAuthKey(), runExecutionContext.getAuthToken());
        substitutionCtx.put(REQUEST, getRequestProperties());
        substitutionCtx.put(RESPONSE, getResponseProperties());
        return substitutionCtx;
    }

    private Map<String, Object> getResponseProperties() {
        final Map<String, Object> responseProperties = new HashMap<>();
        responseProperties.put(BODY, getBodyForSubstitution(httpRunResponse.getOriginalBody()));
        responseProperties.put(CODE, httpRunResponse.getOriginalStatusCode());
        responseProperties.put(HEADERS, httpRunResponse.getOriginalHttpHeaders());
        return responseProperties;
    }

    private Map<String, Object> getRequestProperties() {
        final Map<String, Object> requestProperties = new HashMap<>();
        requestProperties.put(METHOD, httpRunResponse.getRequest().getMethod());
        requestProperties.put(URL, httpRunResponse.getRequest().getUrl());
        Optional.ofNullable(httpRunResponse.getRequest().getBody()).ifPresent(body -> requestProperties.put(BODY, body));
        requestProperties.put(HEADERS, httpRunResponse.getRequest().getHeaders());
        return requestProperties;
    }

    private Object getBodyForSubstitution(final Object body) {
        if (body instanceof String && JSON.isJsonStr(body.toString())) {
            return JSON.readObjectForSubstitution(body.toString());
        } else {
            return body;
        }
    }
}