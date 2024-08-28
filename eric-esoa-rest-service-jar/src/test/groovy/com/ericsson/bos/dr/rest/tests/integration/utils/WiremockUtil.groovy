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

package com.ericsson.bos.dr.rest.tests.integration.utils

import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsBytes
import static com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils.readClasspathResourceAsString
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.delete
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.patch
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching

class WiremockUtil {

    static stubForGetorPost(String url, String method, String requestHeader, String requestBody, int originalResponseStatus, String originalResponseHeader, String originalResponseBody) {
        if ("GET".equalsIgnoreCase(method)) {
            if (requestBody != null) {
                stubFor(get(urlMatching(url))
                        .withHeader("content-Type", equalTo(requestHeader))
                        .withRequestBody(equalToJson(requestBody))
                        .willReturn(aResponse()
                                .withStatus(originalResponseStatus)
                                .withHeader("Content-Type", originalResponseHeader)
                                .withBody(originalResponseBody)))

            } else {
                stubFor(get(urlMatching(url))
                        .withHeader("content-Type", equalTo(requestHeader))
                        .willReturn(aResponse()
                                .withStatus(originalResponseStatus)
                                .withHeader("Content-Type", originalResponseHeader)
                                .withBody(originalResponseBody)))
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            if (requestBody != null) {
                stubFor(post(urlMatching(url))
                        .withHeader("content-Type", equalTo(requestHeader))
                        .withRequestBody(equalToJson(requestBody))
                        .willReturn(aResponse()
                                .withStatus(originalResponseStatus)
                                .withHeader("Content-Type", originalResponseHeader)
                                .withBody(originalResponseBody)))

            } else {
                stubFor(post(urlMatching(url))
                        .withHeader("content-Type", equalTo(requestHeader))
                        .willReturn(aResponse()
                                .withStatus(originalResponseStatus)
                                .withHeader("Content-Type", originalResponseHeader)
                                .withBody(originalResponseBody)))
            }
        }
    }

    static stubForGet(String url, String responseFilePath) {
        stubFor(get(urlMatching(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readClasspathResourceAsString(responseFilePath))))
    }

    static stubForGetAndRequestBody(String url, String requestBody) {
        if (requestBody != null) {
            stubFor(get(urlMatching(url)).withRequestBody(equalToJson(requestBody))
                    .willReturn(aResponse()
                            .withStatus(200)))
        } else {
            stubFor(get(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(200)))
        }
    }

    static stubForGetAndResponseStatus(String url, int status, String responseFilePath) {
        stubFor(get(urlMatching(url))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readClasspathResourceAsString(responseFilePath))))
    }

    static stubForGetAndSingleResponseHeader(String url, String headerValues, String responseFilePath) {
        stubFor(get(urlMatching(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", headerValues)
                        .withHeader("Transfer-Encoding", "chunked")
                        .withBody(readClasspathResourceAsBytes(responseFilePath))))
    }

    static stubForGetAndQueryParams(String url, Integer count, String queryName1, String queryValue1, String queryName2, String queryValue2) {
        switch (count) {
            case 1:
                stubFor(get(urlPathMatching(url)).withQueryParam(queryName1, equalTo(queryValue1))
                        .willReturn(aResponse()
                                .withStatus(200)))
                break
            case 2:
                stubFor(get(urlPathMatching(url)).withQueryParam(queryName1, equalTo(queryValue1)).withQueryParam(queryName2, equalTo(queryValue2))
                        .willReturn(aResponse()
                                .withStatus(200)))
                break
        }
    }

    static stubForGetAndRequestHeaders(String url, Integer count, String headerName1, String headerValue1, String headerName2, String headerValue2) {
        switch (count) {
            case 0:
                stubFor(get(urlPathMatching(url))
                        .willReturn(aResponse()
                                .withStatus(200)))
                break
            case 1:
                stubFor(get(urlPathMatching(url)).withHeader(headerName1, equalTo(headerValue1))
                        .willReturn(aResponse()
                                .withStatus(200)))
                break
            case 2:
                stubFor(get(urlPathMatching(url)).withHeader(headerName1, equalTo(headerValue1)).withHeader(headerName2, equalTo(headerValue2))
                        .willReturn(aResponse()
                                .withStatus(200)))
                break
        }
    }

    static stubForPostAndRequestBody(String url, String requestBody) {
        if (requestBody != null) {
            stubFor(post(urlMatching(url)).withRequestBody(equalToJson(requestBody))
                    .willReturn(aResponse()
                            .withStatus(200)))
        } else {
            stubFor(post(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(200)))
        }
    }

    static stubForPost(String url, String responseFilePath) {
        stubFor(post(urlMatching(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(readClasspathResourceAsString(responseFilePath))))
    }

    static stubForPostAndSingleResponseHeader(String url, String headerKey, String headerValues, String response) {
        stubFor(post(urlMatching(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(headerKey, headerValues)
                        .withBody(response)))
    }

    static stubForPostReturnsNull(String url, String headerKey, String headerValues, String response) {
        stubFor(post(urlMatching(url))
                .willReturn(null))
    }

    static stubForPatchAndRequestBody(String url, String requestBody) {
        if (requestBody != null) {
            stubFor(patch(urlMatching(url)).withRequestBody(equalToJson(requestBody))
                    .willReturn(aResponse()
                            .withStatus(200)))
        } else {
            stubFor(patch(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(200)))
        }
    }

    static stubForPutAndRequestBody(String url, String requestBody) {
        if (requestBody != null) {
            stubFor(put(urlMatching(url)).withRequestBody(equalToJson(requestBody))
                    .willReturn(aResponse()
                            .withStatus(200)))
        } else {
            stubFor(put(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(200)))
        }
    }

    static stubForDelete(String url, String requestBody) {
        if (requestBody != null) {
            stubFor(delete(urlMatching(url)).withRequestBody(equalToJson(requestBody))
                    .willReturn(aResponse()
                            .withStatus(200)))
        } else {
            stubFor(delete(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(200)))
        }
    }
}
