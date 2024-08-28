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

import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readObjectFromClassPathResource
import com.ericsson.bos.dr.rest.service.auth.AuthenticationService
import com.ericsson.bos.dr.rest.service.connectivity.ConnectionProperties
import com.ericsson.bos.dr.rest.service.connectivity.Subsystem
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.ericsson.bos.dr.rest.tests.integration.utils.WiremockUtil
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.beans.factory.annotation.Autowired

class AuthenticationServiceSpec  extends BaseSpec {

    @Autowired
    private AuthenticationService authenticationService


    def "Should authenticate using auth subsystem when referenced in connection properties"() {

        setup: "connection properties reference auth subsystem, from where to retrieve token"
        ConnectionProperties connectionProperties = new ConnectionProperties(authSubsystemName: "auth_subsystem")
        Subsystem originalSubsystem = new Subsystem(connectionProperties: [connectionProperties])

        and: "auth subsystem set"
        Subsystem authSubsystem = readObjectFromClassPathResource(
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json", new TypeReference<List<Subsystem>>() {})[0]
        originalSubsystem.setAuthSubsystem(authSubsystem)

        when: "authenticate"
        String token = authenticationService.authenticate(originalSubsystem)

        then: "token returned from auth subsystem"
        token == "ZW8tdXNlcjphYmNkZUAxMjM0NQ=="
    }

    def "Should authenticate using original subsystem when no auth subsystem referenced"() {

        setup: "connection properties do not reference auth subsystem"
        Subsystem subsystem = readObjectFromClassPathResource(
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json", new TypeReference<List<Subsystem>>() {})[0]

        when: "authenticate"
        String token = authenticationService.authenticate(subsystem)

        then: "token returned from original subsystem"
        token == "ZW8tdXNlcjphYmNkZUAxMjM0NQ=="
    }

}
