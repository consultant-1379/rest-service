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
import com.ericsson.bos.dr.rest.service.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsBytes
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readObjectFromClassPathResource
import com.ericsson.bos.dr.rest.service.auth.BearerHandler
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class BearerHandlerSpec extends BaseSpec {

    @Autowired
    private BearerHandler bearerHandler

    @Unroll
    def "Retrieving bearer token without ssl is successful"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                subsystemFile, new TypeReference<Subsystem>() {})
        ConnectionProperties connectionProperties = subsystem.getConnection()
        and: "stub HttpExecutor to return successful auth response"
        ResponseEntity response = ResponseEntity.ok(readClasspathResourceAsBytes(authResponseFile))
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String authToken = bearerHandler.getAuthToken(subsystem, authKey)

        then: "http executor is invoked with expected properties"
        actualHttpProperties.url == subsystem.getUrl() + connectionProperties.getAuthUrl()
        actualHttpProperties.method == connectionProperties.getAuthMethod()
        actualHttpProperties.body.get() == connectionProperties.getAuthBody()
        actualHttpProperties.headers.toSorted() == connectionProperties.getAuthHeaders()
        actualHttpProperties.encodeUrl == true
        actualHttpProperties.sslVerify == false

        and: "auth token as expected"
        authToken == expectedBearer

        where:
        [subsystemFile, authResponseFile, authKey, expectedBearer] << [getCatalogBearer(), getEcmBearer()]
    }


    def "Retrieving bearer token with ssl is successful"() {
        setup: "prepare connectivity configuration with ssl configured"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/bearer/catalog_bearer_connectivity_config.json", new TypeReference<Subsystem>() {})
        ConnectionProperties connectionProperties = subsystem.getConnection()
        connectionProperties.setSslVerify(true)
        connectionProperties.setSslTrustStoreSecretName("eric-esoa-rest-service-truststore-secret")
        connectionProperties.setSslTrustStoreSecretPassword("password")
        connectionProperties.setSslKeyStoreSecretName("eric-esoa-rest-service-keystore-secret")
        connectionProperties.setSslKeyStoreSecretPassword("password")

        and: "stub HttpExecutor to return successful auth response"
        ResponseEntity response = ResponseEntity.ok(readClasspathResourceAsBytes(authResponseFile))
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String authToken = bearerHandler.getAuthToken(subsystem, authKey)

        then: "http executor is invoked with expected properties"
        actualHttpProperties.url == subsystem.getUrl() + connectionProperties.getAuthUrl()
        actualHttpProperties.sslVerify == true
        actualHttpProperties.keyStoreSecretName == "eric-esoa-rest-service-keystore-secret"
        actualHttpProperties.keyStoreSecretPassword == "password"
        actualHttpProperties.trustStoreSecretName == "eric-esoa-rest-service-truststore-secret"
        actualHttpProperties.trustStoreSecretPassword == "password"

        and: "auth token as expected"
        authToken == expectedBearer

        where:
        [subsystemFile, authResponseFile, authKey, expectedBearer] << [getCatalogBearer()]
    }

    def getCatalogBearer() {
        return ["/auth/bearer/catalog_bearer_connectivity_config.json",
                "/auth/bearer/catalog_bearer_response.json",
                "so_token",
                "aBcD1234.*eFgH5678TOKEN..*"]
    }

    def getEcmBearer() {
        return ["/auth/bearer/ecm_bearer_connectivity_config.json",
                "/auth/bearer/ecm_bearer_response.json",
                "ecm_token",
                "aBcD1234.*eFgH5678TOKEN..*"]
    }

    def "Retrieving bearer token fails, xml response from external system not supported"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/bearer/catalog_bearer_connectivity_config.json", new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return successful auth response with unsupported content-type"
        ResponseEntity response = ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).build()
        httpExecutor.execute(_ as HttpRequest) >> response

        when: "get Auth Token"
        bearerHandler.getAuthToken(subsystem, "so_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.CONTENT_TYPE_IN_HTTPRESPONSE_NOT_SUPPORTED.errorCode
    }

    def "Retrieving bearer auth token fails when response status is not 2xxSuccessful from external system"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/auth/bearer/catalog_bearer_connectivity_config.json", new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return unsuccessful auth response"
        ResponseEntity response = ResponseEntity.status(statusCode).body("error message".bytes)
        httpExecutor.execute(_ as HttpRequest) >> response

        when: "get Auth Token"
        bearerHandler.getAuthToken(subsystem, "so_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.GET_AUTH_TOKEN_ERROR.errorCode
        restServiceException.errorMessage.errorData[2] == "error message"

        where:
        statusCode | _
        400 | _
        500 | _
    }

    @Unroll
    def "Retrieving bearer token fails when mandatory properties are missing in connectivity configuration"() {
        setup: "prepare connectivity configuration"
        ConnectionProperties connectionProperties = new ConnectionProperties(authTokenRef: tokenRef, authUrl: url,authMethod: method)
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])

        when: "get Auth Token"
        bearerHandler.getAuthToken(subsystem, "ecm_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.AUTH_PROPERTIES_MISSING.errorCode

        where:
        tokenRef              | method | url                    | _
        ".status.credentials" | "POST" | ""                     | _
        ".status.credentials" | ""     | "/ecm_service/tokens"  | _
        ""                    | "POST" | "/ecm_service/tokens"  | _
        null                  | null   | null                   | _
    }
}