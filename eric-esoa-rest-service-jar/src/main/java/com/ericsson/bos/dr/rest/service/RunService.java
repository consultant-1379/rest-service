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
package com.ericsson.bos.dr.rest.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.model.resources.ResourceMethodDefinition;
import com.ericsson.bos.dr.rest.service.auth.AuthenticationService;
import com.ericsson.bos.dr.rest.service.connectivity.ConnectivityRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;
import com.ericsson.bos.dr.rest.service.http.HttpExecutor;
import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.run.request.HttpRequestConsumer;
import com.ericsson.bos.dr.rest.service.run.response.HttpResponseConsumer;
import com.ericsson.bos.dr.rest.service.run.response.HttpRunResponse;
import com.ericsson.bos.dr.rest.web.v1.api.model.RunRequestDto;

/**
 * Run service.
 */
@Component
public class RunService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunService.class);

    @Autowired
    private ConnectivityRetriever connectivityRetriever;

    @Autowired
    private ResourceConfigurationService resourceConfigurationService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private List<HttpRequestConsumer> httpRequestConsumers;

    @Autowired
    private List<HttpResponseConsumer> httpResponseConsumers;

    @Autowired
    @Qualifier("connected_system")
    private HttpExecutor httpExecutor;

    /**
     * Executes an API call towards a configured connected system.
     *
     * @param connectivityConfigurationName name of the connected system, as configured in subsystem manager
     * @param resourceConfigurationName     name of the resource configuration to be used
     * @param resource                      name of the resource from the resource configuration to be used
     * @param runRequestDto                 Object containing the dynamic input information to create the contents of the API call
     * @return Object representing the configurable response for the API call
     */
    public ResponseEntity<Object> run(final String connectivityConfigurationName, final String resourceConfigurationName, final String resource,
                                      final RunRequestDto runRequestDto) {

        LOGGER.info("Starting run: connectedSystem={}, resourceConfiguration={}, resource={}, runRequest={}",
                connectivityConfigurationName, resourceConfigurationName, resource, runRequestDto);

        final ResourceMethodDefinition resourceMethodDefinition = resourceConfigurationService.getResourceMethodDefinition
                (resourceConfigurationName, resource, runRequestDto.getMethod());
        final Subsystem subsystem = connectivityRetriever.getSubsystem(connectivityConfigurationName);
        final String authKey = authenticationService.authenticate(subsystem);

        final RunExecutionContext runExecutionContext =
                new RunExecutionContext(runRequestDto, resourceMethodDefinition, subsystem, authKey);

        final HttpRequest httpRequest = new HttpRequest();
        httpRequestConsumers.forEach(consumer -> consumer.apply(httpRequest, runExecutionContext));

        final ResponseEntity<byte[]> httpResponseEntity = httpExecutor.execute(httpRequest, resourceMethodDefinition.getRetryHandlers());
        final HttpRunResponse httpRunResponse = new HttpRunResponse(httpResponseEntity, httpRequest);

        if (!"ORIGINAL".equalsIgnoreCase(runRequestDto.getResponseFormat())) {
            httpResponseConsumers.forEach(consumer -> consumer.apply(httpRunResponse, runExecutionContext));
        }

        return httpRunResponse.asResponseEntity();
    }
}