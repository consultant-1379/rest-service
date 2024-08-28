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
package com.ericsson.bos.dr.rest.tests.integration.cache

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.ericsson.bos.dr.rest.tests.integration.utils.WiremockUtil
import com.ericsson.bos.dr.rest.service.connectivity.ConnectivityRetriever
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import org.springframework.beans.factory.annotation.Autowired

class SubsystemCacheSpec extends BaseSpec {

    @Autowired
    ConnectivityRetriever connectivityRetriever

    def "Get connected system returns value from cache if method has previously been called" () {

        setup: "Stub request to get connected system from subsystem manager"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=subsystem-1",
                "/run/responses/subsystem/subSystemResponse_Bearer.json")

        when: "2 calls to get connected system"
        Subsystem subsystem1 = connectivityRetriever.getSubsystem("subsystem-1")
        Subsystem subsystem2 = connectivityRetriever.getSubsystem("subsystem-1")

        then: "Subsystem-manager is called once"
        wireMock.verify(1 , getRequestedFor(urlEqualTo("/subsystem-manager/v1/subsystems?name=subsystem-1")))

        and: "Same subsystem is returned"
        subsystem1 == subsystem2
    }
}
