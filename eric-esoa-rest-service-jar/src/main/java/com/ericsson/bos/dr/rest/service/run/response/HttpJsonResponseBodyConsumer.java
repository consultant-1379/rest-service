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

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.web.v1.api.model.OutboundPropertyDto;

/**
 * Perform jinja substitution of the http response body using the transformationOutTemplate
 * defined in the resource method definition.
 * If no transformationInTemplate is defined, then no body will be attached to the response.
 */
@Component
public class HttpJsonResponseBodyConsumer implements HttpResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpJsonResponseBodyConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(final HttpRunResponse httpResponse, final RunExecutionContext runExecutionContext) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Original response body: {}", httpResponse.getOriginalBody());
        }
        Optional.ofNullable(runExecutionContext.getMatchedErrorOutboundElseMethodOutbound())
                .map(OutboundPropertyDto::getTransformationOutTemplate).ifPresent(t -> {
                    final Map<String, Object> substitutionCtx = new HttpResponseSubstitutionContext(httpResponse, runExecutionContext).get();
                    final String body = substitutionEngine.render(t, substitutionCtx);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Substituted response body: {}", body);
                    }
                    httpResponse.setTransformedBody(body);
                });
    }
}