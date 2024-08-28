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

import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED;
import java.util.Objects;

import com.ericsson.bos.dr.rest.service.run.RunExecutionContext;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Check the content-type in the http response is supported.
 */
@Component
@Order(1)
public class HttpResponseContentTypeConsumer implements HttpResponseConsumer {

    @Override
    public void apply(HttpRunResponse httpResponse, RunExecutionContext runExecutionContext) {
        final var mediaType = httpResponse.getHttpHeaders().getContentType();
        if (Objects.nonNull(mediaType) && StringUtils.equalsIgnoreCase("xml", mediaType.getSubtype())) {
            throw new RestServiceException(CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED, "xml");
        }
    }
}