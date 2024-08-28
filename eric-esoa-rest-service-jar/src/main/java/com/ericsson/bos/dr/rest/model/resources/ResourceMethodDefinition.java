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
package com.ericsson.bos.dr.rest.model.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.bos.dr.rest.web.v1.api.model.ErrorHandlerDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.InboundPropertyDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.OutboundPropertyDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceConfigurationDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.ResourceMethodPropertyDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.RetryHandlerDto;

/**
 * The definition for an individual resource method in the resource configuration.
 */
public class ResourceMethodDefinition {

    private final ResourceConfigurationDto resourceConfiguration;
    private final ResourceDto resourceDto;
    private String methodName;
    private final ResourceMethodPropertyDto resourceMethod;

    /**
     * ResourceMethodDefinition.
     * @param methodName resource method name
     * @param resourceMethodPropertyDto resource method properties
     * @param resourceDto resource definition
     * @param resourceConfigurationDto resource configuration definition
     */
    public ResourceMethodDefinition(final String methodName, final ResourceMethodPropertyDto resourceMethodPropertyDto,
                                    final ResourceDto resourceDto, final ResourceConfigurationDto resourceConfigurationDto) {
        this.resourceConfiguration = resourceConfigurationDto;
        this.resourceDto = resourceDto;
        this.methodName = methodName;
        this.resourceMethod = resourceMethodPropertyDto;
    }

    public ResourceConfigurationDto getResourceConfiguration() {
        return resourceConfiguration;
    }

    public String getPath() {
        return resourceDto.getPath();
    }

    public ResourceMethodPropertyDto getMethod() {
        return resourceMethod;
    }

    public Optional<InboundPropertyDto> getInbound() {
        return Optional.ofNullable(resourceMethod.getInbound());
    }

    public Optional<OutboundPropertyDto> getOutbound() {
        return Optional.ofNullable(resourceMethod.getOutbound());
    }

    /**
     * Get all <code>RetryHandlerDto</code>. Includes both global and local retry handler definitions.
     * Local retry handler definitions will appear before the global in the returned.
     * @return <code>RetryHandlerDto</code> list
     */
    public List<RetryHandlerDto> getRetryHandlers() {
        final List<RetryHandlerDto> retryHandlers = Optional.ofNullable(resourceMethod.getRetryHandlers()).orElse(new ArrayList<>());
        Optional.ofNullable(resourceConfiguration.getGlobalRetryHandlers()).ifPresent(retryHandlers::addAll);
        return retryHandlers;
    }

    /**
     * Get all <code>ErrorHandlerDto</code>. Includes both global and local error handler definitions.
     * Local error handler definitions will appear before the global in the returned.
     * @return <code>ErrorHandlerDto</code> list
     */
    public List<ErrorHandlerDto> getErrorHandlers() {
        final List<ErrorHandlerDto> errorHandlers = Optional.ofNullable(resourceMethod.getErrorHandlers()).orElse(new ArrayList<>());
        Optional.ofNullable(resourceConfiguration.getGlobalErrorHandlers()).ifPresent(errorHandlers::addAll);
        return errorHandlers;
    }

    /**
     * Get all request headers. Includes both the global and local headers.
     * If same header is defined in both the global and local definitions, then the local
     * headers will take precedence.
     * @return header map
     */
    public Map<String, List<String>> getRequestHeaders() {
        final Map<String, List<String>> requestHeaders =
                Optional.ofNullable(resourceConfiguration.getGlobalRequestHeaders()).orElse(new HashMap<>());
        getInbound().map(InboundPropertyDto::getHeaders).ifPresent(requestHeaders::putAll);
        return requestHeaders;
    }

    public String getMethodName() {
        return methodName;
    }
}