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

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedCaseInsensitiveMap;

import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;

/**
 * Extracts Cookie with name provided by tokenRef from Set-Cookie HttpHeader of a responseEntity.
 */
public class CookieHeaderExtractor {

    private CookieHeaderExtractor() {
    }

    /**
     * Extracts Cookie with name provided by tokenRef from Set-Cookie HttpHeader of a responseEntity.
     *
     * @param responseEntity responseEntity containing cookie in the Set-Cookie HttpHeader
     * @param tokenRef name of the cookie
     * @return cookie
     */
    public static String extractCookieFromResponse(ResponseEntity<byte[]> responseEntity, String tokenRef) {

        final List<String> cookieHeaderList = Optional.ofNullable(responseEntity.getHeaders().get(HttpHeaders.SET_COOKIE))
            .orElseThrow(() -> new RestServiceException(ErrorCode.COOKIE_AUTHENTICATION_FAILED,
                "Set-Cookie header missing in authentication response from external system"));
        final String matchedCookie = getMatchedCookie(tokenRef, cookieHeaderList);
        final Map<String, String> kvMap = cookieStringToMap(matchedCookie);
        return kvMap.get(tokenRef);
    }

    private static String getMatchedCookie(final String tokenRef, final List<String> cookieHeaderList) {
        return cookieHeaderList.stream()
            .filter(cookieHeader -> StringUtils.containsIgnoreCase(cookieHeader, tokenRef))
            .findAny()
            .orElseThrow(
                () -> new RestServiceException(ErrorCode.COOKIE_AUTHENTICATION_FAILED,
                    tokenRef + " is missing in Set-Cookie header in authentication response from external system"));
    }

    private static Map<String, String> cookieStringToMap(final String matchedCookie) {
        return Stream.of(matchedCookie.split(";"))
            .filter(str -> StringUtils.contains(str, "="))
            .map(String::trim)
            .map(str -> StringUtils.splitByWholeSeparator(str, "="))
            .collect(
                toMap(str -> str[0], str -> str[1], (k1, k2) -> k2, LinkedCaseInsensitiveMap::new));
    }

}
