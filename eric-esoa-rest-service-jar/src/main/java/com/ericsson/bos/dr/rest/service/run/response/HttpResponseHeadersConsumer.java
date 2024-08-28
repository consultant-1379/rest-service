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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.LinkedMultiValueMap;

import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.substitution.SubstitutionEngine;
import com.ericsson.bos.dr.rest.web.v1.api.model.OutboundPropertyDto;

/**
 * Perform jinja substitution of the http response header values defined in the resource configuration.
 * The headers consist of a combination of the defined global response headers and local response headers.
 */
@Component
public class HttpResponseHeadersConsumer implements HttpResponseConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponseHeadersConsumer.class);

    @Autowired
    private SubstitutionEngine substitutionEngine;

    @Override
    public void apply(final HttpRunResponse httpResponse, final RunExecutionContext runExecutionContext) {
        final Map<String, Object> substitutionCtx = new HttpResponseSubstitutionContext(httpResponse, runExecutionContext).get();
        final LinkedCaseInsensitiveMap<List<String>> configHeaders = new LinkedCaseInsensitiveMap<>();
        Optional.ofNullable(runExecutionContext.getResourceMethod().getResourceConfiguration().getGlobalResponseHeaders()).ifPresent(h -> {
            h.values().removeAll(Collections.singleton(null));
            configHeaders.putAll(h);
        });
        Optional.ofNullable(runExecutionContext.getMatchedErrorOutboundElseMethodOutbound()).map(OutboundPropertyDto::getHeaders).ifPresent(h -> {
            h.values().removeAll(Collections.singleton(null));
            configHeaders.putAll(h);
        });

        if (!configHeaders.isEmpty()) {
            final var resultHeaders = new HttpHeaders();
            resultHeaders.addAll(new LinkedMultiValueMap<>(substituteResponseHeaders(configHeaders, substitutionCtx)));
            httpResponse.setHttpHeaders(resultHeaders);
            LOGGER.debug("Response headers: {}", resultHeaders);
        }
    }

    private  LinkedCaseInsensitiveMap<List<String>> substituteResponseHeaders(final LinkedCaseInsensitiveMap<List<String>> headers,
                                                                              final Map<String, Object> substitutionCtx) {
        final LinkedCaseInsensitiveMap<List<String>> resultHeaderMap = new LinkedCaseInsensitiveMap<>();
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            final List<String> substitutedValueList = entry.getValue().stream()
                            .map(originalHeaderValue -> substitutionEngine.render(originalHeaderValue, substitutionCtx))
                            .toList();
            resultHeaderMap.put(entry.getKey(), substitutedValueList);
        }
        return resultHeaderMap;
    }
}
