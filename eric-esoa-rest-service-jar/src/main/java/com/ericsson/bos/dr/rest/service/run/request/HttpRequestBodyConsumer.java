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

import java.util.Map;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.web.v1.api.model.InboundPropertyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Perform jinja substitution of http request body using the transformationInTemplate defined
 * in the resource method definition.
 * If no transformationInTemplate is defined then the body provided in the run request is set on the http request.
 */
//The order is set to 1 so the body is transformed first, enabling the body to be used in the
//substitution context of other consumers. One example is a requirement to provide a message integrity check
//for the body by setting Content-MD5 header.
@Order(1)
@Component
public class HttpRequestBodyConsumer implements HttpRequestConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestBodyConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(HttpRequest httpRequest, RunExecutionContext runExecutionContext) {
        final Object runRequestBody = runExecutionContext.getRunRequest().getBody();
        final String transformInTemplate = runExecutionContext.getResourceMethod().getInbound()
                .map(InboundPropertyDto::getTransformationInTemplate).orElse(null);
        if (runRequestBody != null) {
            httpRequest.setBody(runRequestBody);
        } else if (transformInTemplate != null) {
            final Map<String, Object> substitutionCtx = new HttpRequestSubstitutionContext(httpRequest, runExecutionContext).get();
            final String body = substitutionEngine.render(transformInTemplate, substitutionCtx);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Substituted request body: {}", body);
            }
            httpRequest.setBody(body);
        }
    }
}