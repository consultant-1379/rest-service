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
package com.ericsson.bos.dr.rest.service.connectivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * Connected system
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subsystem {

    public static final String AUTH_TYPE_BASIC_AUTH = "BasicAuth";
    public static final String AUTH_TYPE_BASIC_AUTH_TOKEN = "BasicAuthToken";
    public static final String AUTH_TYPE_BEARER = "Bearer";
    public static final String AUTH_TYPE_COOKIE = "Cookie";
    public static final String AUTH_TYPE_NOAUTH = "NoAuth";
    public static final String AUTH_USER_NAME = "auth.username";
    public static final String AUTH_PASSWORD = "auth.password";
    public static final String AUTH_URL = "auth.url";
    public static final String AUTH_METHOD = "auth.method";
    public static final String AUTH_TOKENREF = "auth.tokenRef";

    private String name;
    private String url;
    private List<ConnectionProperties> connectionProperties;

    private Subsystem authSubsystem;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ConnectionProperties> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(List<ConnectionProperties> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public ConnectionProperties getConnection() {
        return getConnectionProperties().get(0);
    }

    /**
     * check is sslVerify is true or false
     * @return Boolean value of sslVerify - true or false
     */
    public Boolean isSslVerify() {
        if (getConnection().getSslVerify() != null) {
            return getConnection().getSslVerify();
        } else {
            return false;
        }
    }

    /**
     * Get subsystem authentication request url.
     * @return auth url
     */
    public String getJoinedAuthUrl() {
        final String baseUrl = getUrl();
        final String authUrl = getConnection().getAuthUrl();
        return StringUtils.startsWithIgnoreCase(authUrl, "http") ? authUrl : StringUtils.join(baseUrl,authUrl);
    }

    public Optional<Subsystem> getAuthSubsystem() {
        return Optional.ofNullable(authSubsystem);
    }

    public void setAuthSubsystem(final Subsystem authSubsystem) {
        this.authSubsystem = authSubsystem;
    }

    /**
     * Returns auth key.
     *
     * @return auth key.
     */
    public String getAuthKey() {
        final Subsystem subsystemWithAuthKey = getAuthSubsystem().orElse(this);
        return subsystemWithAuthKey
                .getConnection()
                .getAuthKey();
    }

    @Override
    public String toString() {
        return "Subsystem{" +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", connectionProperties=" + connectionProperties +
                '}';
    }
}
