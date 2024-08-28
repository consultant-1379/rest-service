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
package com.ericsson.bos.dr.rest.service.run;

import java.util.Optional;

import com.ericsson.bos.dr.rest.model.resources.ResourceMethodDefinition;
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;
import com.ericsson.bos.dr.rest.web.v1.api.model.OutboundPropertyDto;
import com.ericsson.bos.dr.rest.web.v1.api.model.RunRequestDto;

/**
 * Execution context for invocation of a run request.
 */
public class RunExecutionContext {

    private final RunRequestDto runRequest;
    private final ResourceMethodDefinition resourceMethod;
    private final Subsystem subsystem;
    private final String authToken;
    private OutboundPropertyDto matchedErrorHandlerOutbound;

    /**
     * RunExecutionContext
     * @param runRequest run request
     * @param resourceMethod resource method definition
     * @param subsystem target subsystem
     * @param authToken authToken
     */
    public RunExecutionContext(RunRequestDto runRequest, ResourceMethodDefinition resourceMethod,
                               Subsystem subsystem, String authToken) {
        this.runRequest = runRequest;
        this.resourceMethod = resourceMethod;
        this.subsystem = subsystem;
        this.authToken = authToken;
    }

    public RunRequestDto getRunRequest() {
        return runRequest;
    }

    public ResourceMethodDefinition getResourceMethod() {
        return resourceMethod;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setMatchedErrorOutbound(OutboundPropertyDto outbound) {
        this.matchedErrorHandlerOutbound = outbound;
    }

    /**
     * Get <code>OutboundPropertyDto</code>. If <code>matchedErrorHandlerOutbound</code> has been set, then return it otherwise
     * return the local <code>OutboundPropertyDto</code> on the resource method.
     * @return OutboundPropertyDto
     */
    public OutboundPropertyDto getMatchedErrorOutboundElseMethodOutbound() {
        return Optional.ofNullable(matchedErrorHandlerOutbound)
                .orElseGet(() -> resourceMethod.getOutbound().orElse(null));
    }
}