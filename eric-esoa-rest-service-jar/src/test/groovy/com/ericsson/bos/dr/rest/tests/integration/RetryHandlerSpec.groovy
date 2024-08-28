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


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.hamcrest.Matchers.matchesRegex
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.test.web.servlet.ResultActions

import com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils
import com.ericsson.bos.dr.rest.tests.integration.utils.JsonUtils
import com.ericsson.bos.dr.rest.tests.integration.utils.WiremockUtil
import com.ericsson.bos.dr.rest.web.v1.api.model.RunRequestDto
import com.fasterxml.jackson.core.type.TypeReference
import com.github.tomakehurst.wiremock.client.WireMock

class RetryHandlerSpec extends BaseSpec {

    def "Resource requests are retried according to retry handler definition for http response errors"() {

        setup: "Upload resource configuration"
        uploadResource()

        and: "stub subsystem manager request to return connected system"
        String subsystemName = "external-system"
        WiremockUtil.stubForGet("/subsystem-manager/v1/subsystems\\?name=${subsystemName}",
                "/run/responses/subsystem/subSystemResponse_BasicAuth.json")

        and: "stub external system request to get resource"
        stubFor(get(urlMatching("/resource1"))
                .willReturn(aResponse()
                .withStatus(500)
                .withHeader("header1", responseHeader)
                .withBody(responseBody)))

        when: "execute run"
        ResultActions result = executeRunRequest()

        then: "expected response is returned"
        result.andExpect(status().is(500)).andExpect(content().string(responseBody))

        and: "expected number of requests executed"
        WireMock.verify(requestCount, getRequestedFor(urlEqualTo("/resource1")))

        where:
        responseCode | responseBody            | responseHeader | requestCount
        400          | "error1"                | ""             | 2
        500          | "unmatched"             | ""             | 1
        500          | "error1"                | ""             | 2
        500          | "error2"                | ""             | 3
        500          | "error1"                | "1"            | 2
        500          | ""                      | "2"            | 3
        500          | "globalError1"          | ""             | 2
        500          | '{"prop1": "error1"}'   | ""             | 2
        500          | '[{"prop1": "error1"}]' | ""             | 2
    }

    def "Resource requests are retried according to retry handler definition for connection timeout"() {

        setup: "Upload resource configuration"
        uploadResource()

        and: "stub subsystem manager request to return unreachable subsystem"
        List subsystems = IOUtils.readObjectFromClassPathResource("/run/responses/subsystem/subSystemResponse_BasicAuth.json",
                new TypeReference<List>() {})
        subsystems[0]['url'] = "http://1.2.3.4:8080"
        stubFor(get(urlMatching("/subsystem-manager/v1/subsystems\\?name=external-system"))
                .willReturn(aResponse()
                .withStatus(200)
                .withBody(JsonUtils.toJsonString(subsystems))))

        when: "execute run"
        ResultActions result = executeRunRequest()

        then: "expected response, referring to connection timeout, is returned"
        result.andExpect(status().is(500)).andExpect(content()
                .string("{\"errorMessage\":\"Internal server error: ConnectTimeoutException: connection timed out after 1000 ms: /1.2.3.4:8080.\",\"errorCode\":\"RS-500\"}"))

        and: "number of requests executed is consistent with having retried the request"
        webClientRequestsRecorder.getRequestCount("http://1.2.3.4:8080/resource1") == 2
    }

    def "Resource requests are retried according to retry handler definition when connection refused"() {

        setup: "Upload resource configuration"
        uploadResource()

        and: "stub subsystem manager request to return unreachable subsystem (localhost URL with an unusual port)"
        List subsystems = IOUtils.readObjectFromClassPathResource("/run/responses/subsystem/subSystemResponse_BasicAuth.json",
                new TypeReference<List>() {})
        subsystems[0]['url'] = "http://localhost:55555"
        stubFor(get(urlMatching("/subsystem-manager/v1/subsystems\\?name=external-system"))
                .willReturn(aResponse()
                .withStatus(200)
                .withBody(JsonUtils.toJsonString(subsystems))))

        when: "execute run"
        ResultActions result = executeRunRequest()

        then: "expected response, referring to connection refusal, is returned"
        result.andExpect(status().is(500)).andExpect(content()
                .string(matchesRegex("\\{\"errorMessage\":\"Internal server error: ConnectException: .*Connection refused.*\",\"errorCode\":\"RS-500\"\\}")))

        and: "number of requests executed is consistent with having retried the request"
        webClientRequestsRecorder.getRequestCount("http://localhost:55555/resource1") == 2
    }

    void uploadResource() {
        resourceConfigurationTestSteps.uploadResourceConfiguration("/run/resource-configuration/retry_handling.yml", "resource1")
    }

    ResultActions executeRunRequest() {
        RunRequestDto runRequestDto = new RunRequestDto(method: "GET", responseFormat: "original", body: null, inputs: [:])
        return runServiceTestSteps
                .executeRunResult("external-system", "EXTERNAL_SYSTEM", "resource1", runRequestDto)
    }
}