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

import java.util.Objects;

/**
 * Token data
 */
public class TokenData {
    private String token;
    private Long expireSeconds;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public Long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(final Long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var tokenData = (TokenData) o;
        return Objects.equals(token, tokenData.token) && Objects.equals(expireSeconds, tokenData.expireSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, expireSeconds);
    }

    @Override
    public String toString() {
        return "TokenData{" +
               "token='" + token + '\'' +
               ", expireSeconds=" + expireSeconds +
               '}';
    }
}
