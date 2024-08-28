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
package com.ericsson.bos.dr.rest.service.auth;

import static com.ericsson.bos.dr.rest.service.exceptions.ErrorCode.CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;

import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import com.ericsson.bos.dr.rest.service.utils.JQ;
import com.ericsson.bos.dr.rest.service.utils.JSON;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Extracts token from the body of a responseEntity.
 */
public class TokenExtractor {

    private TokenExtractor() {
    }

    /**
     * Extracts token from the body of a responseEntity,
     *
     * @param responseEntity responseEntity containing body with token
     * @param tokenRef name of the property containing the token
     * @return token
     */
    public static String extractTokenFromResponse(ResponseEntity<byte[]> responseEntity, String tokenRef) {

        final String originalResponseContent = Optional.ofNullable(responseEntity.getBody())
                .map(String::new).orElse(null);

        final String contentType = Optional.ofNullable(responseEntity.getHeaders().getContentType())
            .map(MimeType::getSubtype).orElse("json");

        switch (contentType) {
            case "xml":
                throw new RestServiceException(CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED, "xml");
            case "json":
            default:
                return JQ.query(tokenRef, JSON.read(originalResponseContent, JsonNode.class)).getObject().toString();
        }
    }
}
