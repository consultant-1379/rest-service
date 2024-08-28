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
package com.ericsson.bos.dr.rest.service.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Http Request properties.
 */
public class HttpRequest {

    private String url;
    private String method;
    private Object body;
    private HttpHeaders headers;
    private Integer readTimeoutSeconds = 60;
    private Integer writeTimeoutSeconds = 60;
    private Integer connectTimeoutSeconds = 10;
    private Boolean sslVerify = false;
    private String trustStoreSecretName;
    private String trustStoreSecretPassword;
    private String keyStoreSecretName;
    private String keyStoreSecretPassword;
    private Boolean encodeUrl = true;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public Optional<Object> getBody() {
        return Optional.ofNullable(body);
    }

    public void setBody(final Object body) {
        this.body = body;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(final HttpHeaders headers) {
        this.headers = headers;
    }

    /**
     * Set headers.
     *
     * @param headers
     *         headers
     */
    public void setHeaders(final Map<String, List<String>> headers) {
        if (headers != null) {
            final MultiValueMap<String, String> multiValueHeadersMap = new LinkedMultiValueMap<>();
            multiValueHeadersMap.putAll(headers);
            this.headers = new HttpHeaders(multiValueHeadersMap);
        }
    }

    public Integer getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(final Integer readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public Integer getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public void setWriteTimeoutSeconds(final Integer writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

    public Integer getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(final Integer connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public Boolean getSslVerify() {
        return sslVerify;
    }

    public void setSslVerify(final Boolean sslVerify) {
        this.sslVerify = sslVerify;
    }

    public String getTrustStoreSecretName() {
        return trustStoreSecretName;
    }

    public void setTrustStoreSecretName(final String trustStoreSecretName) {
        this.trustStoreSecretName = trustStoreSecretName;
    }

    public String getTrustStoreSecretPassword() {
        return trustStoreSecretPassword;
    }

    public void setTrustStoreSecretPassword(final String trustStoreSecretPassword) {
        this.trustStoreSecretPassword = trustStoreSecretPassword;
    }

    public String getKeyStoreSecretName() {
        return keyStoreSecretName;
    }

    public void setKeyStoreSecretName(final String keyStoreSecretName) {
        this.keyStoreSecretName = keyStoreSecretName;
    }

    public String getKeyStoreSecretPassword() {
        return keyStoreSecretPassword;
    }

    public void setKeyStoreSecretPassword(final String keyStoreSecretPassword) {
        this.keyStoreSecretPassword = keyStoreSecretPassword;
    }

    public boolean getEncodeUrl() {
        return encodeUrl;
    }

    public void setEncodeUrl(final Boolean encodeUrl) {
        this.encodeUrl = encodeUrl;
    }

    @Override
    public String toString() {
        return "HttpRequest{"
                + "method='" + method + "'"
                + ", url='" + url + "'"
                + ", body={" + getBody().orElse("null") + "}"
                + ", headers=" + headers
                + ", connectTimeoutSeconds=" + connectTimeoutSeconds
                + ", readTimeoutSeconds=" + readTimeoutSeconds
                + ", writeTimeoutSeconds=" + writeTimeoutSeconds
                + "}";
    }
}