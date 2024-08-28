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

import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsBytesWithSubstitution
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readObjectFromClassPathResource

import com.ericsson.bos.dr.rest.service.auth.CookieHandler
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.ericsson.bos.dr.rest.tests.integration.utils.WiremockUtil
import com.fasterxml.jackson.core.type.TypeReference
import com.ericsson.bos.dr.rest.service.connectivity.ConnectionProperties
import com.ericsson.bos.dr.rest.service.http.HttpRequest

import org.springframework.http.ResponseEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import spock.lang.Unroll

class CookieHandlerSpec extends BaseSpec {

    @Autowired
    private CookieHandler cookieHandler

    @Unroll
    def "Retrieving cookie token without ssl is successful"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource(subsystemFile, new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return successful auth response with cookie header set"
        ResponseEntity response = ResponseEntity.status(status).header(HttpHeaders.SET_COOKIE, responseCookieHeader).build()
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String cookie = cookieHandler.getAuthToken(subsystem, authKey)

        then: "http executor is invoked with expected properties"
        actualHttpProperties.url == subsystem.getUrl() + subsystem.getConnection().getAuthUrl()
        actualHttpProperties.method == subsystem.getConnection().getAuthMethod()
        actualHttpProperties.body == Optional.empty()
        actualHttpProperties.headers.toSorted() == subsystem.getConnection().getAuthHeaders()
        actualHttpProperties.encodeUrl == true
        actualHttpProperties.sslVerify == false

        and: "auth token as expected"
        cookie == expectedCookie

        where:
        [subsystemFile, responseCookieHeader, authKey, expectedCookie, status] << [getCtsCookie(), getEnmCookie()]
    }

    @Unroll
    def "Retrieving cookie token with ssl is successful"() {
        setup: "prepare connectivity configuration with ssl configured"
        Subsystem subsystem = readObjectFromClassPathResource(subsystemFile,
                new TypeReference<Subsystem>() {})
        ConnectionProperties connectionProperties = subsystem.getConnection()
        connectionProperties.setSslVerify(true)
        connectionProperties.setSslTrustStoreSecretName("eric-esoa-rest-service-truststore-secret")
        connectionProperties.setSslTrustStoreSecretPassword("password")
        connectionProperties.setSslKeyStoreSecretName("eric-esoa-rest-service-keystore-secret")
        connectionProperties.setSslKeyStoreSecretPassword("password")

        and: "stub HttpExecutor to return successful auth response with cookie header set"
        ResponseEntity response = ResponseEntity.status(status).header(HttpHeaders.SET_COOKIE, responseCookieHeader).build()
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String cookie = cookieHandler.getAuthToken(subsystem, authKey)

        then: "http executor is invoked with expected properties"
        actualHttpProperties.url == subsystem.getUrl() + connectionProperties.getAuthUrl()
        actualHttpProperties.sslVerify == true
        actualHttpProperties.keyStoreSecretName == "eric-esoa-rest-service-keystore-secret"
        actualHttpProperties.keyStoreSecretPassword == "password"
        actualHttpProperties.trustStoreSecretName == "eric-esoa-rest-service-truststore-secret"
        actualHttpProperties.trustStoreSecretPassword == "password"

        and: "auth token as expected"
        cookie == expectedCookie

        where:
        [subsystemFile, responseCookieHeader, authKey, expectedCookie, status] << [getCtsCookie(), getEnmCookie()]
    }

    def getCtsCookie() {
        return ["/auth/cookie/cts_cookie_connectivity_config.json",
                "JSESSIONID=aBcD1234.*eFgH5678TOKEN..*; Path=/; Max-Age=36000; Expires=Mon, 08 Aug 2022 17:32:15 GMT; Secure; HttpOnly; SameSite=Lax",
                "so_token",
                "aBcD1234.*eFgH5678TOKEN..*",
                200]
    }

    def getEnmCookie() {
        return ["/auth/cookie/enm_cookie_connectivity_config.json",
                "iPlanetDirectoryPro=aBcD1234.*eFgH5678TOKEN..*; Path=/; Version=0; Secure; HttpOnly",
                "enm_token",
                "aBcD1234.*eFgH5678TOKEN..*",
                302]
    }


    def "Auth body is mapped to multiValueMap when content-type is x-www-form-urlencoded."() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readClasspathResourceAsBytesWithSubstitution(
                "/auth/cookie/enm_cookie_connectivity_config_encode_body.json", new TypeReference<Subsystem>() {},
                ["header": "{\\\"Content-Type\\\": [\\\"application/x-www-form-urlencoded\\\"]}", "body": authBody])

        and: "stub HttpExecutor to return successful auth response with cookie header set"
        ResponseEntity response = ResponseEntity.status(302).header(HttpHeaders.SET_COOKIE,
                "iPlanetDirectoryPro=aBcD1234.*eFgH5678TOKEN..*; Path=/; Version=0; Secure; HttpOnly").build()
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String cookie = cookieHandler.getAuthToken(subsystem, "enm_token")

        then: "authBody is mapped to multiValueMap"
        actualHttpProperties.body.get().toString() == responseBody

        and: "auth token as expected"
        cookie == "aBcD1234.*eFgH5678TOKEN..*"

        where:
        authBody                                                      | responseBody
        "Token1=adm&Token2=pass"                                      | "{Token1=[adm], Token2=[pass]}"
        "{\\\"Token1\\\": \\\"adm\\\", \\\"Token2\\\": \\\"pass\\\"}" | "{Token1=[adm], Token2=[pass]}"
        "{}"                                                          | "{}"
    }

    def "Raw body is passed when content-type is not x-www-form-urlencoded"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readClasspathResourceAsBytesWithSubstitution(
                "/auth/cookie/enm_cookie_connectivity_config_encode_body.json",
                new TypeReference<Subsystem>() {},["header": authHeader, "body": authBody])

        and: "stub HttpExecutor to return successful auth response with cookie header set"
        ResponseEntity response = ResponseEntity.status(302).header(HttpHeaders.SET_COOKIE,
                "iPlanetDirectoryPro=aBcD1234.*eFgH5678TOKEN..*; Path=/; Version=0; Secure; HttpOnly").build()
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        String cookie = cookieHandler.getAuthToken(subsystem, "enm_token")

        then: "raw authBody is passed"
        actualHttpProperties.body.get().toString() == responseBody

        and: "auth token as expected"
        cookie == "aBcD1234.*eFgH5678TOKEN..*"

        where:
        authHeader                                           | authBody                           | responseBody
        "{\\\"Content-Type\\\": []}"                         | "Token1=ad#m&Token2=pass"           | "Token1=ad#m&Token2=pass"
        "{}"                                                 | "Token1=ad#m&Token2=pass"           | "Token1=ad#m&Token2=pass"
    }

    def "Should throw exception when x-www-form-urlencoded body is not in valid format"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readClasspathResourceAsBytesWithSubstitution(
                "/auth/cookie/enm_cookie_connectivity_config_encode_body.json", new TypeReference<Subsystem>() {},
                ["header": "{\\\"content-type\\\": [\\\"application/x-www-form-urlencoded\\\"]}", "body": body])

        and: "stub HttpExecutor to return successful auth response with cookie header set"
        ResponseEntity response = ResponseEntity.status(302).header(HttpHeaders.SET_COOKIE,
                "iPlanetDirectoryPro=aBcD1234.*eFgH5678TOKEN..*; Path=/; Version=0; Secure; HttpOnly").build()
        HttpRequest actualHttpProperties = null
        httpExecutor.execute(_ as HttpRequest) >> { actualHttpProperties = it[0]; response }

        when: "get Auth Token"
        cookieHandler.getAuthToken(subsystem, "enm_token")

        then: "throw exception"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.INVALID_AUTH_BODY.errorCode

        where:
        body                  | _
        "Token1admToken2pass" | _
        "Token1="             | _
        "=pass"               | _
    }

    @Unroll
    def "Retrieving cookie token fails when mandatory properties are missing in connectivity configuration"() {
        setup: "prepare connectivity configuration"
        ConnectionProperties connectionProperties = new ConnectionProperties(authTokenRef: tokenRef, authUrl: url,authMethod: method)
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])

        when: "get Auth Token"
        cookieHandler.getAuthToken(subsystem, "so_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.AUTH_PROPERTIES_MISSING.errorCode

        where:
        tokenRef     | method | url         | _
        "JSESSIONID" | "POST" | ""          | _
        "JSESSIONID" | ""     | "/auth/v1"  | _
        ""           | "POST" | "/auth/v1"  | _
        null         | null   | null        | _
    }

    def "Retrieving cookie auth token fails when response status is not 2xxSuccessful from external system"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource("/auth/cookie/cts_cookie_connectivity_config.json",
                new TypeReference<Subsystem>() {})

        and: "stub HttpExecutor to return unsuccessful auth response"
        ResponseEntity response = ResponseEntity.status(statusCode).body("error message".bytes)
        httpExecutor.execute(_ as HttpRequest) >> response

        when: "get Auth Token"
        cookieHandler.getAuthToken(subsystem, "so_token")

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
    def "Retrieving cookie token fails when call to external system to retrieve cookie returns header without correct cookie information"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = readObjectFromClassPathResource("/auth/cookie/cts_cookie_connectivity_config.json",
                new TypeReference<Subsystem>() {})

        and: "stub external system request to get token"
        WiremockUtil.stubForPostAndSingleResponseHeader("/auth/v1/login", headerKey, headerValues, "body_not_used")

        and: "stub HttpExecutor to return successful auth response with configured headers"
        ResponseEntity response = ResponseEntity.ok().header(headerKey, headerValues).build()
        httpExecutor.execute(_ as HttpRequest) >> response

        when: "get Auth Token"
        cookieHandler.getAuthToken(subsystem, "so_token")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.COOKIE_AUTHENTICATION_FAILED.errorCode

        where:
        headerKey     | headerValues
        "Accept"      | "JSESSIONID=aBcD1234.*eFgH5678TOKEN..*"
        "Set-Cookie2" | "JSESSIONID=aBcD1234.*eFgH5678TOKEN..*"
        "Set-Cookie"  | "NOT_JSESSION_ID=aBcD1234.*eFgH5678TOKEN..*"
    }
}