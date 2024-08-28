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
package com.ericsson.bos.dr.rest.tests.unit.connectivity

import com.ericsson.bos.dr.rest.service.connectivity.ConnectionProperties
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.fasterxml.jackson.core.type.TypeReference
import spock.lang.Specification
import spock.lang.Unroll

import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readObjectFromClassPathResource

class SubsystemSpec extends Specification {

    @Unroll
    def "should return correct auth url"() {

        setup: "Create subsystem"
        ConnectionProperties connectionProperties = new ConnectionProperties(authUrl: authUrl)
        Subsystem subsystem = new Subsystem()
        subsystem.setUrl(externalSystemUrl)
        subsystem.setConnectionProperties([connectionProperties])

        when: "call made to get auth url"
        String actualAuthUrl = subsystem.getJoinedAuthUrl()

        then: "correct auth url is returned. If auth.url starts with http then use this, else if auth.url is a path then concat subsystem url and path"
        actualAuthUrl == expectedAuthUrl

        where:
        externalSystemUrl                | authUrl                                   | expectedAuthUrl
        "https://externalsystemhostname" | "/authpath/endpoint"                      | "https://externalsystemhostname/authpath/endpoint"
        "https://externalsystemhostname" | "https://externalsystemhostname/endpoint" | "https://externalsystemhostname/endpoint"
        "https://externalsystemhostname" | "https://othersystemhostname/endpoint"    | "https://othersystemhostname/endpoint"
        "https://externalsystemhostname" | "hTTp://othersystemhostname/endpoint"     | "hTTp://othersystemhostname/endpoint"
    }

    @Unroll
    def "should return authKey from auth subsystem if set"() {

        setup: "subsystem"
        ConnectionProperties connectionProperties = new ConnectionProperties(authKey: "authKeyOne")
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])
        subsystem.setAuthSubsystem(auth_subsystem)

        when: "get auth key"
        String actualAuthKey = subsystem.getAuthKey()

        then: "correct auth key returned"
        actualAuthKey == expected_auth_key

        where:
        auth_subsystem                                                                         | expected_auth_key
        null                                                                                   | "authKeyOne"
        new Subsystem(connectionProperties: [new ConnectionProperties(authKey: "authKeyTwo")]) | "authKeyTwo"
    }

    @Unroll
    def "should return correct sslVerify value"() {

        setup: "subsystem"
        ConnectionProperties connectionProperties = new ConnectionProperties(sslVerify: ssl_verify_value)
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])

        when: "is ssl verify"
        boolean isSslVerifyActual = subsystem.isSslVerify()

        then: "correct sslVerify value returned"
        isSslVerifyActual == expected_ssl_verifyy

        where:
        ssl_verify_value | expected_ssl_verifyy
        true             | true
        false            | false
        null             | false
    }

    @Unroll
    def "should be able to process get subsystem response with '.' and '_' in keys"() {

        setup: "get subsystem details"
        Subsystem subsystem = readObjectFromClassPathResource(ssmResponse, new TypeReference<Subsystem>() {})

        when: "retrieve connection properties from connection properties"
        String actualAuthUserName = subsystem.connection.authUsername
        String actualAuthPassword = subsystem.connection.authPassword
        String actualAuthType = subsystem.connection.authType
        String actualAuthUrl = subsystem.connection.authUrl
        String actualAuthMethod = subsystem.connection.authMethod
        String actualAuthBody = subsystem.connection.authBody
        String actualAuthExpireSeconds = subsystem.connection.authExpireSeconds
        String actualAuthKey = subsystem.connection.authKey
        String actualAuthSubsytemName = subsystem.connection.authSubsystemName
        String actualAuthTokenRef = subsystem.connection.authTokenRef
        Map<String, List<String>> actualAuthHeaders = subsystem.connection.authHeaders
        Integer actualClientConnectionTimeoutSeconds = subsystem.connection.clientConnectionTimeoutSeconds
        Integer actualTimeoutSeconds = subsystem.connection.readTimeoutSeconds
        Integer actualWriteTimeoutSeconds = subsystem.connection.writeTimeoutSeconds
        Boolean actualSslVerify = subsystem.connection.sslVerify
        String actualSslTrustStoreSecretName = subsystem.connection.sslTrustStoreSecretName
        String actualSslTrustStorePassword = subsystem.connection.sslTrustStoreSecretPassword
        String actualSslKeyStoreSecretName = subsystem.connection.sslKeyStoreSecretName
        String actualSslKeyStorePassword = subsystem.connection.sslKeyStoreSecretPassword

        then: "verify connection properties are as expected"
        actualAuthUserName == expectedAuthUsername
        actualAuthPassword == expectedAuthPassword
        actualAuthType == expectedAuthType
        actualAuthUrl == expectedAuthUrl
        actualAuthMethod == expectedAuthMethod
        actualAuthBody == expectedAuthBody
        actualAuthExpireSeconds == expectedAuthExpireSeconds
        actualAuthKey == expectedAuthKey
        actualAuthSubsytemName == expectedAuthSubsystemName
        actualAuthTokenRef == expectedAuthTokenRef
        actualAuthHeaders == expectedAuthHeaders
        actualClientConnectionTimeoutSeconds == expectedClientConnectionTimeoutSeconds
        actualTimeoutSeconds == expectedReadTimeoutSeconds
        actualWriteTimeoutSeconds == expectedWriteTimeoutSeconds
        actualSslVerify == expectedSslVerify
        actualSslTrustStoreSecretName == expectedSslTrustStoreSecretName
        actualSslTrustStorePassword == expectedSslTrustStoreSecretPassword
        actualSslKeyStoreSecretName == expectedSslKeyStoreSecretName
        actualSslKeyStorePassword == expectedSslKeyStoreSecretPassword

        where:
        [ssmResponse,
        expectedAuthUsername,
        expectedAuthPassword,
        expectedAuthType,
        expectedAuthUrl,
        expectedAuthMethod,
        expectedAuthBody,
        expectedAuthExpireSeconds,
        expectedAuthKey,
        expectedAuthSubsystemName,
        expectedAuthTokenRef,
        expectedAuthHeaders,
        expectedClientConnectionTimeoutSeconds,
        expectedReadTimeoutSeconds,
        expectedWriteTimeoutSeconds,
        expectedSslVerify,
        expectedSslTrustStoreSecretName,
        expectedSslTrustStoreSecretPassword,
        expectedSslKeyStoreSecretName,
        expectedSslKeyStoreSecretPassword ] << [ getSubsystemWithPeriods(), getSubsystemWithUnderscores() ]
    }

    def getSubsystemWithPeriods() {
        return ["/ssm-response-parsing/subsystem_with_periods_in_keys.json",
                "user",
                "pwd",
                "Bearer",
                "/token?username=user1&password=password123&grant_type=password&client_id=eo&client_secret=secret123",
                "POST",
                "username=user1&password=password123&grant_type=password&client_id=eo&client_secret=secret123",
                "300",
                "target_token",
                "",
                ".access_token",
                ["content-type":["application/x-www-form-urlencoded"],"accept":["*/*"]],
                10,
                60,
                60,
                false,
                "trustStoreName",
                "trustStorePwd",
                "keyStoreName",
                "keyStorePwd"
        ]
    }

    def getSubsystemWithUnderscores() {
        return ["/ssm-response-parsing/subsystem_with_underscores_in_keys.json",
                "user",
                "pwd",
                "Bearer",
                "/token?username=user1&password=password123&grant_type=password&client_id=eo&client_secret=secret123",
                "POST",
                "username=user1&password=password123&grant_type=password&client_id=eo&client_secret=secret123",
                "300",
                "target_token",
                "",
                ".access_token",
                ["content-type":["application/x-www-form-urlencoded"],"accept":["*/*"]],
                10,
                60,
                60,
                true,
                "trustStoreName",
                "trustStorePwd",
                "keyStoreName",
                "keyStorePwd"
        ]
    }
}
