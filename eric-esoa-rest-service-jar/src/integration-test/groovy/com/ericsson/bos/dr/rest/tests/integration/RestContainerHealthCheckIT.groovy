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

package com.ericsson.bos.dr.rest.tests.integration

import org.springframework.http.ResponseEntity
import spock.lang.Ignore

@Ignore("This test to be enabled by https://eteamproject.internal.ericsson.com/browse/ESOA-16334")
class RestContainerHealthCheckIT extends BaseIT {


    def "Rest docker container should be up and able to receive requests"() {

        when: 'Get feature packs'
        ResponseEntity<List> response = get(
                "/rest-service/v1/resource-configurations", List.class)

        then: 'Expect 200 http status code if container is up able to receive requests'
        assert response.getStatusCode().is2xxSuccessful()
    }


}
