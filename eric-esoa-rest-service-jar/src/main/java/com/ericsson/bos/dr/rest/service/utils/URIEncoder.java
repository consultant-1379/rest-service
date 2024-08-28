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
package com.ericsson.bos.dr.rest.service.utils;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Alternative to DefaultUriBuilderFactory and UriComponentsBuilder for constructing URIs, as neither
 * can encode a query parameter whose value is a json.
 */
public class URIEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(URIEncoder.class);

    private URIEncoder() {}

    /**
     * Construct an encoded URI from the supplied uri string.
     * Encoding is performed on the path segments and the query parameter values.
     * For example given the uri string 'http://localhost:8080/path1/path2?param1="v1"&param2="v2", the
     * encoded string will be 'http://localhost:8080/path1/path2?param1=%22v1%22&param2=%22v2%22.
     * @param uriString uri string
     * @return encoded URI
     */
    public static URI fromString(final String uriString) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Original uri: {}", uriString);
        }
        final var uriComponentsBuilder = UriComponentsBuilder.fromUriString(uriString);
        final var encodedUriComponentsBuilder = uriComponentsBuilder.cloneBuilder();
        final var originalUri = uriComponentsBuilder.build();
        encodePathSegments(originalUri, encodedUriComponentsBuilder);
        encodeQueryParamValues(originalUri, encodedUriComponentsBuilder);
        final var encodedUri = encodedUriComponentsBuilder.build(true).toUri();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Encoded uri: {}", encodedUri);
        }
        return encodedUri;
    }

    private static void encodePathSegments(final UriComponents originalUri, final UriComponentsBuilder encodedUriBuilder) {
        final var encodedPath = originalUri.getPathSegments().stream()
                .map(s -> encode(s, UTF_8)).collect(Collectors.joining("/"));
        encodedUriBuilder.replacePath(encodedPath);
    }

    private static void encodeQueryParamValues(final UriComponents originalUri, final UriComponentsBuilder encodedUriBuilder) {
        if (StringUtils.isNotEmpty(originalUri.getQuery())) {
            originalUri.getQueryParams().entrySet().stream()
                    .forEach(queryParamEntry -> {
                        final List<String> values = queryParamEntry.getValue().stream().map(v -> v == null ? null : encode(v, UTF_8))
                                .collect(ArrayList::new, List::add, ArrayList::addAll);
                        encodedUriBuilder.replaceQueryParam(queryParamEntry.getKey(), values);
                    });
        }
    }
}