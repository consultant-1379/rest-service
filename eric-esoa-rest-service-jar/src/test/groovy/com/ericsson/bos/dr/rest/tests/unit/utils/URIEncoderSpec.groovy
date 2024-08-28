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
package com.ericsson.bos.dr.rest.tests.unit.utils

import com.ericsson.bos.dr.rest.service.utils.URIEncoder
import spock.lang.Specification

class URIEncoderSpec extends Specification {

    def "uri string is encoded"() {
        when: "encode uri string"
        String result = URIEncoder.fromString(uri).toString()

        then: "string is encoded as expected"
        result == encodedUri

        where:
        uri                                                             | encodedUri
        "http://localhost:8080/path1/path2"                             | "http://localhost:8080/path1/path2"
        "http://localhost:8080/path1/path2?"                            | "http://localhost:8080/path1/path2"
        "http://localhost:8080/path1/path2?param1=v1&param2=v2"         | "http://localhost:8080/path1/path2?param1=v1&param2=v2"
        "http://localhost:8080/path1/path2?param1=\"v1\"&param2=\"v2\"" | "http://localhost:8080/path1/path2?param1=%22v1%22&param2=%22v2%22"
        "http://localhost:8080/path1/path2?param1=v1,v2,v3"             | "http://localhost:8080/path1/path2?param1=v1%2Cv2%2Cv3"
        "http://localhost:8080/path1/path2?param1"                      | "http://localhost:8080/path1/path2?param1"
        "http://localhost:8080/path1/path2?param1&param1=v1"            | "http://localhost:8080/path1/path2?param1&param1=v1"
        "http://localhost:8080?filters=[{\"name\":\"object1\"}]"        | "http://localhost:8080?filters=%5B%7B%22name%22%3A%22object1%22%7D%5D"
        "http://localhost:8080/path1\$/path2\$?"                        | "http://localhost:8080/path1%24/path2%24"
    }
}