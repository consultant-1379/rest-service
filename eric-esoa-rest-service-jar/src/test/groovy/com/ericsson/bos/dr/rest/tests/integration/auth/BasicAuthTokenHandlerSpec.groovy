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
package com.ericsson.bos.dr.rest.tests.integration.auth

import com.ericsson.bos.dr.rest.service.connectivity.ConnectionProperties
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.ericsson.bos.dr.rest.service.http.HttpRequest
import org.springframework.http.ResponseEntity

import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsBytes
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readObjectFromClassPathResource
import com.ericsson.bos.dr.rest.service.auth.BasicAuthTokenHandler
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class BasicAuthTokenHandlerSpec extends BaseSpec {

    @Autowired
    private BasicAuthTokenHandler basicAuthTokenHandler

    def "Retrieving auth token without ssl is successful"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/basic-auth-token/ecm_basic_auth_token_connectivity_config.json", new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return successful auth response"
        ResponseEntity response = ResponseEntity.ok(readClasspathResourceAsBytes("/auth/basic-auth-token/basic_auth_token_response.json"))
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String authToken = basicAuthTokenHandler.getAuthToken(subsystem, "ecm_token")

        then: "http executor is invoked with expected properties"
        actualHttpProperties.url == subsystem.getUrl() + subsystem.getConnection().getAuthUrl()
        actualHttpProperties.method == subsystem.getConnection().getAuthMethod()
        actualHttpProperties.body == Optional.empty()
        actualHttpProperties.headers.toSorted() ==
                [Authorization: ['Basic ZWNtYWRtaW46bGV0bWVpbg==']] + subsystem.getConnection().getAuthHeaders()
        actualHttpProperties.encodeUrl == true
        actualHttpProperties.sslVerify == false

        and: "auth token as expected"
        authToken == "aBcD1234.*eFgH5678TOKEN..*"
    }

    def "Retrieving auth token with ssl is successful"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/basic-auth-token/ecm_basic_auth_token_connectivity_config.json", new TypeReference<Subsystem>() {})
        ConnectionProperties connectionProperties = subsystem.getConnection()
        connectionProperties.setSslVerify(true)
        connectionProperties.setSslTrustStoreSecretName("eric-esoa-rest-service-truststore-secret")
        connectionProperties.setSslTrustStoreSecretPassword("password")
        connectionProperties.setSslKeyStoreSecretName("eric-esoa-rest-service-keystore-secret")
        connectionProperties.setSslKeyStoreSecretPassword("password")

        and: "stub HttpExecutor to return successful auth response"
        ResponseEntity response = ResponseEntity.ok(readClasspathResourceAsBytes("/auth/basic-auth-token/basic_auth_token_response.json"))
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String authToken = basicAuthTokenHandler.getAuthToken(subsystem, "ecm_token")

        then: "http executor is invoked with expected properties"
        actualHttpProperties.url == subsystem.getUrl() + connectionProperties.getAuthUrl()
        actualHttpProperties.sslVerify == true
        actualHttpProperties.keyStoreSecretName == "eric-esoa-rest-service-keystore-secret"
        actualHttpProperties.keyStoreSecretPassword == "password"
        actualHttpProperties.trustStoreSecretName == "eric-esoa-rest-service-truststore-secret"
        actualHttpProperties.trustStoreSecretPassword == "password"

        and: "auth token as expected"
        authToken == "aBcD1234.*eFgH5678TOKEN..*"
    }

    @Unroll
    def "Retrieving auth token fails when mandatory properties are missing in connectivity configuration"() {
        setup: "prepare connectivity configuration"
        ConnectionProperties connectionProperties = new ConnectionProperties(
                authUsername: username, authPassword: password, authTokenRef: tokenRef, authUrl: url,authMethod: method)
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])

        when: "get Auth Token"
        basicAuthTokenHandler.getAuthToken(subsystem, "ecm_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.AUTH_PROPERTIES_MISSING.errorCode

        where:
        username     | password     | tokenRef              | method | url
        "myUsername" | "myPassword" | ".status.credentials" | "POST" | ""
        "myUsername" | "myPassword" | ".status.credentials" | ""     | "/auth/v1"
        "myUsername" | "myPassword" | ""                    | "POST" | "/auth/v1"
        "myUsername" | ""           | ".status.credentials" | "POST" | "/auth/v1"
        ""           | "myPassword" | ".status.credentials" | "POST" | "/auth/v1"
        null         | null         | null                  | null   | null
    }

    @Unroll
    def "Retrieving bearer auth token fails when response status is not 2xxSuccessful from external system"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/basic-auth-token/ecm_basic_auth_token_connectivity_config.json", new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return unsuccessful auth response"
        ResponseEntity response = ResponseEntity.status(statusCode).body("error message".bytes)
        httpExecutor.execute(_ as HttpRequest) >> response

        when: "get Auth Token"
        basicAuthTokenHandler.getAuthToken(subsystem, "so_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.GET_AUTH_TOKEN_ERROR.errorCode
        restServiceException.errorMessage.errorData[2] == "error message"

        where:
        statusCode | _
        400        | _
        500        | _
    }

    def "Retrieving bearer auth token fails when response is null from external system"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/basic-auth-token/ecm_basic_auth_token_connectivity_config.json", new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return null auth response"
        httpExecutor.execute(_ as HttpRequest) >> null

        when: "get Auth Token"
        basicAuthTokenHandler.getAuthToken(subsystem, "so_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.GET_AUTH_TOKEN_ERROR.errorCode
        restServiceException.errorMessage.errorData[1] == "Authentication response is empty."
    }

}