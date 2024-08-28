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

import com.ericsson.bos.dr.rest.service.connectivity.ConnectionProperties;
import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import org.springframework.stereotype.Component;

/**
 * Set timeout properties (connection, read, write)  in the http request as per the subsystem configuration.
 * If no values defined in the subsystem configuration, then defaults are used.
 */
@Component
public class HttpRequestTimeoutsConsumer implements HttpRequestConsumer {

    @Override
    public void apply(HttpRequest httpRequest, RunExecutionContext runExecutionContext) {
        final ConnectionProperties connectionProperties = runExecutionContext.getSubsystem().getConnection();
        connectionProperties.getClientConnectionTimeoutSeconds();
        httpRequest.setConnectTimeoutSeconds(connectionProperties.getClientConnectionTimeoutSeconds());
        httpRequest.setReadTimeoutSeconds(connectionProperties.getReadTimeoutSeconds());
        httpRequest.setWriteTimeoutSeconds(connectionProperties.getWriteTimeoutSeconds());
    }
}