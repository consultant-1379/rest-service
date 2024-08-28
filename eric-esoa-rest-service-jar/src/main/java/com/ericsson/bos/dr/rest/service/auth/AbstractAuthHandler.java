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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;

import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.bos.dr.rest.service.cache.TokenCache;
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem;
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode;
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException;
import com.ericsson.bos.dr.rest.service.http.HttpExecutor;
import com.ericsson.bos.dr.rest.service.http.HttpRequest;
import com.ericsson.bos.dr.rest.service.utils.JSON;

/**
 * Abstract auth handler
 */
@Component
public abstract class AbstractAuthHandler implements AuthHandler {

    private static final  String BODY_PATTERN = "^(?>\\w+=[^&]++)(?>&\\w+=[^&]++)*$";

    @Autowired
    TokenCache tokenCache;

    @Autowired
    @Qualifier("connected_system")
    HttpExecutor httpExecutor;

    @Override
    public String getAuthKey(final Subsystem subsystem) {
        return subsystem.getConnection().getAuthKey();
    }

    @Override
    public String getAuthToken(final Subsystem subsystem, final String authKey) {
        validate(subsystem);
        final var tokenData = tokenCache.getTokenData(authKey, () -> generateTokenData(subsystem));
        return tokenData.getToken();
    }

    /**
     * Validates that connectivity configuration contains all auth properties required for generation of TokenData
     * @param subsystem an external system
     */
    protected void validate(final Subsystem subsystem) { }

    /**
     * Generates TokenData
     * @param subsystem system
     * @return the TokenData
     */
    protected abstract TokenData generateTokenData(Subsystem subsystem);

    /**
     * Get responseEntity from the execution of the subsystem http auth request.
     * @param subsystem system
     * @param basicAuth include basic authentication header
     * @return responseEntity
     */
    protected ResponseEntity<byte[]> executeAuthRequest(Subsystem subsystem, boolean basicAuth) {
        final var responseEntity = httpExecutor.execute(createSubsystemAuthRequest(subsystem, basicAuth));
        if (Objects.isNull(responseEntity)) {
            throw new RestServiceException(ErrorCode.GET_AUTH_TOKEN_ERROR, subsystem.getName(), "Authentication response is empty.", "");
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful() && !responseEntity.getStatusCode().is3xxRedirection()) {
            throw new RestServiceException(ErrorCode.GET_AUTH_TOKEN_ERROR, subsystem.getName(),
                    responseEntity.getStatusCode().toString(),
                    Optional.ofNullable(responseEntity.getBody()).map(String::new).orElse(null));
        }
        return responseEntity;
    }

    /**
     * Create subsystem authentication request.
     * When header is x-www-form-url-encoded, body is stored into a multiValueMap which will be automatically encoded by webClient.
     * @param subsystem subsystem 
     * @param basicAuth include basic authentication header
     * @return HttpRequestProperties
     */
    protected HttpRequest createSubsystemAuthRequest(final Subsystem subsystem,
                                                     final boolean basicAuth) {
        final var httpRequest = new HttpRequest();
        final var connectionProperties = subsystem.getConnection();
        httpRequest.setUrl(subsystem.getJoinedAuthUrl());
        httpRequest.setMethod(connectionProperties.getAuthMethod());
        httpRequest.setEncodeUrl(true);
        final HttpHeaders headers = new HttpHeaders();
        headers.putAll(connectionProperties.getAuthHeaders());
        if (Boolean.TRUE.equals(basicAuth)) {
            final String userName = connectionProperties.getAuthUsername();
            final String password = connectionProperties.getAuthPassword();
            final var basicAuthToken = Base64.getEncoder().encodeToString(StringUtils.join(userName, ":", password).getBytes(UTF_8));
            headers.put(HttpHeaders.AUTHORIZATION,
                    Collections.singletonList(StringUtils.join("Basic", " ", basicAuthToken)));
        }
        httpRequest.setHeaders(headers);
        final String authBody = connectionProperties.getAuthBody();
        if (StringUtils.isNotBlank(authBody) && urlEncodeBody(headers)) {
            httpRequest.setBody(toMultiValueMap(authBody));
        } else {
            httpRequest.setBody(authBody);
        }
        httpRequest.setSslVerify(connectionProperties.getSslVerify());
        if (Boolean.TRUE.equals(connectionProperties.getSslVerify())) {
            httpRequest.setKeyStoreSecretName(connectionProperties.getSslKeyStoreSecretName());
            httpRequest.setKeyStoreSecretPassword(connectionProperties.getSslKeyStoreSecretPassword());
            httpRequest.setTrustStoreSecretName(connectionProperties.getSslTrustStoreSecretName());
            httpRequest.setTrustStoreSecretPassword(connectionProperties.getSslTrustStoreSecretPassword());
        }
        return httpRequest;
    }

    private MultiValueMap<String, String> toMultiValueMap(String authBody) {
        final MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        if (JSON.isJsonStr(authBody)) {
            final Map<String, String> bodyMap = JSON.read(authBody, Map.class);
            bodyMap.forEach(multiValueMap::add);
        } else {
            final var matcher = Pattern.compile(BODY_PATTERN).matcher(authBody);
            if (matcher.find()) {
                Arrays.stream(authBody.split("&"))
                        .map(pair -> pair.split("=", 2))
                        .forEach(pair -> multiValueMap.add(pair[0], pair[1]));
            } else {
                throw new RestServiceException(ErrorCode.INVALID_AUTH_BODY, authBody);
            }
        }
        return multiValueMap;
    }

    private boolean urlEncodeBody(Map<String, List<String>> headers) {
        final List<String> authHeaderContentType = headers.get("content-type");
        if (authHeaderContentType == null || authHeaderContentType.isEmpty()) {
            return false;
        }
        return authHeaderContentType.get(0).equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }
}