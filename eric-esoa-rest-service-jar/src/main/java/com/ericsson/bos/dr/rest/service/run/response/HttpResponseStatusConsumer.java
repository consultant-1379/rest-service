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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.web.v1.api.model.OutboundPropertyDto;

/**
 * Perform jinja substitution of the response status if defined in the outbound method definition
 * in the resource configuration.
 */
@Component
public class HttpResponseStatusConsumer implements HttpResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseStatusConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(final HttpRunResponse httpResponse, final RunExecutionContext runExecutionContext) {
        LOGGER.debug("Original response status: {}", httpResponse.getStatusCode());
        Optional.ofNullable(runExecutionContext.getMatchedErrorOutboundElseMethodOutbound()).map(OutboundPropertyDto::getCode).ifPresent(code -> {
            final Map<String, Object> substitutionCtx = new HttpResponseSubstitutionContext(httpResponse, runExecutionContext).get();
            final String transformedCode = substitutionEngine.render(code, substitutionCtx);
            if (StringUtils.isNotEmpty(transformedCode)) {
                httpResponse.setStatusCode(HttpStatus.valueOf(Integer.parseInt(transformedCode)).value());
                LOGGER.debug("Substituted response status: {}", httpResponse.getStatusCode());
            }
        });
    }
}