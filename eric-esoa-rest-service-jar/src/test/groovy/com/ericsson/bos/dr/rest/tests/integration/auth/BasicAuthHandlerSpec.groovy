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

import static java.nio.charset.StandardCharsets.UTF_8
import com.ericsson.bos.dr.rest.service.auth.BasicAuthHandler
import com.ericsson.bos.dr.rest.service.cache.TokenCache
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException
import com.ericsson.bos.dr.rest.service.connectivity.ConnectionProperties
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class BasicAuthHandlerSpec extends BaseSpec {

    @Autowired
    private BasicAuthHandler basicAuthHandler

    @Autowired
    private TokenCache tokenCache

    def "Retrieving auth token is successful"() {
        setup: "prepare connectivity configuration"
        ConnectionProperties connectionProperties = new ConnectionProperties(authUsername: "myUsername", authPassword: "myPassword")
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])

        when: "get Auth Token"
        String authToken = basicAuthHandler.getAuthToken(subsystem, "basicAuthKey_1")

        then: "auth token as expected"
        assert authToken == Base64.getEncoder().encodeToString("myUsername:myPassword".getBytes(UTF_8))
    }

    @Unroll
    def "Retrieving auth token fails when mandatory properties are missing in connectivity configuration"() {
        setup: "prepare connectivity configuration"
        ConnectionProperties connectionProperties = new ConnectionProperties(authUsername: username, authPassword: password)
        Subsystem subsystem = new Subsystem(connectionProperties: [connectionProperties])

        when: "get Auth Token"
        basicAuthHandler.getAuthToken(subsystem, "basicAuthKey_2")

        then: "RestServiceException is thrown"
        RestServiceException restServiceException = thrown(RestServiceException)
        restServiceException.errorMessage.errorCode == ErrorCode.AUTH_PROPERTIES_MISSING.errorCode

        where:
        username     | password
        "myUsername" | null
        null         | "myPassword"
        null         | null
    }
}
