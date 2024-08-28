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

import com.ericsson.bos.dr.rest.service.utils.JSON;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;
import java.util.Map;

/**
 * Connection properties of a connected system
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionProperties {

    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("ssl.verify")
    @JsonAlias("ssl_verify")
    private Boolean sslVerify = false;
    @JsonProperty("ssl.trustStoreSecretName")
    @JsonAlias("ssl_trustStoreSecretName")
    private String sslTrustStoreSecretName;
    @JsonProperty("ssl.trustStoreSecretPassword")
    @JsonAlias("ssl_trustStoreSecretPassword")
    private String sslTrustStoreSecretPassword;
    @JsonProperty("ssl.keyStoreSecretName")
    @JsonAlias("ssl_keyStoreSecretName")
    private String sslKeyStoreSecretName;
    @JsonProperty("ssl.keyStoreSecretPassword")
    @JsonAlias("ssl_keyStoreSecretPassword")
    private String sslKeyStoreSecretPassword;
    @JsonProperty("auth.username")
    @JsonAlias("auth_username")
    private String authUsername;
    @JsonProperty("auth.password")
    @JsonAlias("auth_password")
    private String authPassword;
    @JsonProperty("auth.type")
    @JsonAlias("auth_type")
    private String authType;
    @JsonProperty("auth.url")
    @JsonAlias("auth_url")
    private String authUrl;
    @JsonProperty("auth.method")
    @JsonAlias("auth_method")
    private String authMethod;
    @JsonProperty("auth.body")
    @JsonAlias("auth_body")
    private String authBody;
    @JsonProperty("auth.expireSeconds")
    @JsonAlias("auth_expireSeconds")
    private String authExpireSeconds;
    @JsonProperty("auth.key")
    @JsonAlias("auth_key")
    private String authKey;
    @JsonProperty("auth.subsystemName")
    @JsonAlias("auth_subsystemName")
    private String authSubsystemName;
    @JsonProperty("auth.tokenRef")
    @JsonAlias("auth_tokenRef")
    private String authTokenRef;
    @JsonProperty("client.connectTimeoutSeconds")
    @JsonAlias("client_connectTimeoutSeconds")
    private Integer clientConnectionTimeoutSeconds;
    @JsonProperty("client.readTimeoutSeconds")
    @JsonAlias("client_readTimeoutSeconds")
    private Integer readTimeoutSeconds;
    @JsonProperty("client.writeTimeoutSeconds")
    @JsonAlias("client_writeTimeoutSeconds")
    private Integer writeTimeoutSeconds;

    private Map<String, List<String>> authHeaders;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSslVerify() {
        return sslVerify;
    }

    public void setSslVerify(Boolean sslVerify) {
        this.sslVerify = sslVerify;
    }

    public String getSslTrustStoreSecretName() {
        return sslTrustStoreSecretName;
    }

    public void setSslTrustStoreSecretName(String sslTrustStoreSecretName) {
        this.sslTrustStoreSecretName = sslTrustStoreSecretName;
    }

    public String getSslTrustStoreSecretPassword() {
        return sslTrustStoreSecretPassword;
    }

    public void setSslTrustStoreSecretPassword(String sslTrustStoreSecretPassword) {
        this.sslTrustStoreSecretPassword = sslTrustStoreSecretPassword;
    }

    public String getSslKeyStoreSecretName() {
        return sslKeyStoreSecretName;
    }

    public void setSslKeyStoreSecretName(String sslKeyStoreSecretName) {
        this.sslKeyStoreSecretName = sslKeyStoreSecretName;
    }

    public String getSslKeyStoreSecretPassword() {
        return sslKeyStoreSecretPassword;
    }

    public void setSslKeyStoreSecretPassword(String sslKeyStoreSecretPassword) {
        this.sslKeyStoreSecretPassword = sslKeyStoreSecretPassword;
    }

    public String getAuthUsername() {
        return authUsername;
    }

    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public Map<String, List<String>> getAuthHeaders() {
        return authHeaders;
    }

    @JsonSetter("auth.headers")
    @JsonAlias("auth_headers")
    public void setAuthHeaders(String authHeaders) {
        this.authHeaders = JSON.read(authHeaders, Map.class);
    }

    public String getAuthBody() {
        return authBody;
    }

    public void setAuthBody(String authBody) {
        this.authBody = authBody;
    }

    public String getAuthExpireSeconds() {
        return authExpireSeconds;
    }

    public void setAuthExpireSeconds(String authExpireSeconds) {
        this.authExpireSeconds = authExpireSeconds;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getAuthSubsystemName() {
        return authSubsystemName;
    }

    public void setAuthSubsystemName(final String authSubsystemName) {
        this.authSubsystemName = authSubsystemName;
    }

    public String getAuthTokenRef() {
        return authTokenRef;
    }

    public void setAuthTokenRef(String authTokenRef) {
        this.authTokenRef = authTokenRef;
    }

    public Integer getClientConnectionTimeoutSeconds() {
        return clientConnectionTimeoutSeconds != null ? clientConnectionTimeoutSeconds : 10;
    }

    public void setClientConnectionTimeoutSeconds(Integer clientConnectionTimeoutSeconds) {
        this.clientConnectionTimeoutSeconds = clientConnectionTimeoutSeconds;
    }

    public Integer getReadTimeoutSeconds() {
        return readTimeoutSeconds != null  ? readTimeoutSeconds : 60;
    }

    public void setReadTimeoutSeconds(Integer readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public Integer getWriteTimeoutSeconds() {
        return writeTimeoutSeconds != null ? writeTimeoutSeconds : 60;
    }

    public void setWriteTimeoutSeconds(Integer writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "ConnectionProperties{" +
               "username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", sslVerify=" + sslVerify +
               ", sslTrustStoreSecretName='" + sslTrustStoreSecretName + '\'' +
               ", sslTrustStoreSecretPassword='" + sslTrustStoreSecretPassword + '\'' +
               ", sslKeyStoreSecretName='" + sslKeyStoreSecretName + '\'' +
               ", sslKeyStoreSecretPassword='" + sslKeyStoreSecretPassword + '\'' +
               ", authUsername='" + authUsername + '\'' +
               ", authPassword='" + authPassword + '\'' +
               ", authType='" + authType + '\'' +
               ", authUrl='" + authUrl + '\'' +
               ", authMethod='" + authMethod + '\'' +
               ", authBody='" + authBody + '\'' +
               ", authExpireSeconds='" + authExpireSeconds + '\'' +
               ", authKey='" + authKey + '\'' +
               ", authSubsystemName='" + authSubsystemName + '\'' +
               ", authTokenRef='" + authTokenRef + '\'' +
               ", clientConnectionTimeoutSeconds=" + clientConnectionTimeoutSeconds +
               ", readTimeoutSeconds=" + readTimeoutSeconds +
               ", writeTimeoutSeconds=" + writeTimeoutSeconds +
               ", authHeaders=" + authHeaders +
               '}';
    }
}
