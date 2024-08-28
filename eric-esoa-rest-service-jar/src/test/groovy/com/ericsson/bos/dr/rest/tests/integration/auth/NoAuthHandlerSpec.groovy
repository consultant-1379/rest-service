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

import com.ericsson.bos.dr.rest.service.auth.NoAuthHandler
import com.ericsson.bos.dr.rest.service.cache.TokenCache
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import org.springframework.beans.factory.annotation.Autowired

class NoAuthHandlerSpec extends BaseSpec {

    @Autowired
    private NoAuthHandler noAuthHandler

    @Autowired
    private TokenCache tokenCache

    def "Retrieving auth token is successful"() {
        setup: "prepare connectivity configuration"
        Subsystem subsystem = new Subsystem()

        when: "get Auth Token"
        String authToken = noAuthHandler.getAuthToken(subsystem, "noAuthTokenKey")

        then: "auth token as expected"
        assert authToken == null
    }


}
