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
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Set Authentication properties in the http request as per the subsystem auth configuration.
 */
@Component
public class HttpRequestAuthConsumer implements HttpRequestConsumer {

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(HttpRequest httpRequest, RunExecutionContext runExecutionContext) {
        final ConnectionProperties connectionProperties = runExecutionContext.getSubsystem().getConnection();
        if (Boolean.TRUE.equals(runExecutionContext.getSubsystem().isSslVerify())) {
            httpRequest.setSslVerify(true);
            httpRequest.setTrustStoreSecretName(connectionProperties.getSslTrustStoreSecretName());
            httpRequest.setTrustStoreSecretPassword(connectionProperties.getSslTrustStoreSecretPassword());
            httpRequest.setKeyStoreSecretName(connectionProperties.getSslKeyStoreSecretName());
            httpRequest.setKeyStoreSecretPassword(connectionProperties.getSslKeyStoreSecretPassword());
        }
    }
}