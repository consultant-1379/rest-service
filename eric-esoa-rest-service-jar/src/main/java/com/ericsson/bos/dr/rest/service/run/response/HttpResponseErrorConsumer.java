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

import com.ericsson.bos.dr.rest.model.resources.ResourceMethodDefinition;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.web.v1.api.model.ErrorHandlerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Check if the http response matches a condition in a global or local error handler defined
 * in the resource configuration.
 * If matched, then updates the <code>RunExecutionContext</code> with the matched error handler.
 */
@Component
@Order(2)
public class HttpResponseErrorConsumer implements HttpResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseErrorConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(HttpRunResponse httpResponse, RunExecutionContext runExecutionContext) {
        final Map<String, Object> substitutionCtx = new HttpResponseSubstitutionContext(httpResponse, runExecutionContext).get();
        getMatchedErrorHandler(runExecutionContext.getResourceMethod(), substitutionCtx)
                .ifPresent(eh -> runExecutionContext.setMatchedErrorOutbound(eh.getOutbound()));
    }

    private Optional<ErrorHandlerDto> getMatchedErrorHandler(final ResourceMethodDefinition resourceMethod,
                                                             final Map<String, Object> substitutionContext) {
        final Optional<ErrorHandlerDto> errorHandlerDto = resourceMethod.getErrorHandlers().stream()
                .filter(eh -> Boolean.parseBoolean(substitutionEngine.render(eh.getErrorCondition(), substitutionContext)))
                .findFirst();
        if (errorHandlerDto.isPresent()) {
            LOGGER.debug("Found matching error handler: {}", errorHandlerDto.get());
        }
        return errorHandlerDto;
    }
}