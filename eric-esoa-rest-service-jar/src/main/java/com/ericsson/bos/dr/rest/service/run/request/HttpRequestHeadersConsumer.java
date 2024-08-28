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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;

/**
 * Perform jinja substitution of the http request header values defined in the resource configuration.
 * The headers consist of a combination of the defined global request headers and local request headers.
 * If a header value is not a jinja expression then the value will be unchanged.
 */
@Component
public class HttpRequestHeadersConsumer implements HttpRequestConsumer {

    private  static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestHeadersConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(final HttpRequest httpRequest, final RunExecutionContext runExecutionContext) {
        final Map<String, List<String>> requestHeaders = runExecutionContext.getResourceMethod().getRequestHeaders();
        Optional.ofNullable(requestHeaders).ifPresent(headers -> {
            final Map<String, Object> substitutionCtx = new HttpRequestSubstitutionContext(httpRequest, runExecutionContext).get();
            for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
                final List<String> substitutedHeaderValues = entry.getValue().stream()
                        .filter(Objects::nonNull)
                        .map(headerValue -> substitutionEngine.render(headerValue, substitutionCtx))
                        .toList();
                headers.replace(entry.getKey(), substitutedHeaderValues);
            }
        });
        LOGGER.debug("Request headers: {}", requestHeaders);
        httpRequest.setHeaders(new HttpHeaders(CollectionUtils.toMultiValueMap(requestHeaders)));
    }
}