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
package com.ericsson.bos.dr.rest.tests.unit.http

import org.springframework.boot.context.properties.EnableConfigurationProperties

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


import com.ericsson.bos.dr.rest.service.utils.URIEncoder
import com.ericsson.bos.dr.rest.service.http.HttpExecutor
import com.ericsson.bos.dr.rest.service.http.HttpRequest
import com.github.tomakehurst.wiremock.WireMockServer

import io.netty.resolver.dns.DnsErrorCauseException
import io.netty.resolver.dns.DnsNameResolverTimeoutException
import spock.lang.Specification

@ContextConfiguration(classes = HttpExecutorTestConfig.class)
@TestPropertySource(properties = ["service.http-executor.retry.max-attempts=1", "service.http-executor.retry.delay=1"], locations="classpath:application.properties")
class HttpExecutorSpec extends Specification {

    @Autowired
    @Qualifier("connected_system")
    HttpExecutor httpExecutor

    static String wiremockPort

    static WireMockServer wireMockServer = new WireMockServer(options().dynamicPort())

    def setupSpec() {
        wireMockServer.start()
        configureFor(wireMockServer.port())
        wiremockPort = Integer.toString(wireMockServer.port())
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def cleanup() {
        wireMockServer.resetAll()
    }

    def "Execute get request with content-type JSON"() {
        setup: "Create request properties"
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("http://localhost:${wiremockPort}/rest-api/some-endpoint".toString())
        httpRequest.setMethod("GET")
        httpRequest.setBody('{"username":"administrator"}')
        httpRequest.setHeaders(["Accept": ["application/json"]])
        httpRequest.setEncodeUrl(true)

        and: "Configure expected rest call from the http executor"
        wireMockServer.stubFor(get(urlEqualTo("/rest-api/some-endpoint"))
                .withHeader("Accept", matching("application/json"))
                .withRequestBody(equalToJson('{"username":"administrator"}'))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withBody("some response")));

        when: "Execute GET http request"
        ResponseEntity response = httpExecutor.execute(httpRequest)

        then: "Response as expected"
        assert new String(response.body) == "some response"
        assert response.statusCode == HttpStatus.valueOf(statusCode)

        where:
        statusCode | _
        200 | _
        500 | _
    }

    def "url is encoded when encodeUrl is set to true when executing get request"() {
        setup: "Create request properties"
        String queryParams = "?Token1=adm&Token2=pass^&*"
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("http://localhost:${wiremockPort}/rest-api/some-endpoint".toString() + queryParams)
        httpRequest.setMethod("GET")
        httpRequest.setBody(null)
        httpRequest.setHeaders(["Accept": ["application/json"]])
        httpRequest.setReadTimeoutSeconds(1)
        httpRequest.setWriteTimeoutSeconds(1)
        httpRequest.setConnectTimeoutSeconds(1)
        httpRequest.setEncodeUrl(true)

        and: "Configure expected rest call from the http executor"
        String encodedQueryParams = URIEncoder.fromString(queryParams)
        wireMockServer.stubFor(get(urlEqualTo("/rest-api/some-endpoint" + encodedQueryParams))
                .withHeader("Accept", matching("application/json"))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withBody("some response")));

        when: "Execute GET http request"
        ResponseEntity response = httpExecutor.execute(httpRequest)

        then: "Response as expected"
        assert new String(response.body) == "some response"
        assert response.statusCode == HttpStatus.valueOf(statusCode)

        where:
        statusCode | _
        200 | _
        500 | _
    }

    def "MultiValueMap body is encoded when content-type is www-form-urlencoded."() {
        setup: "Create request properties"
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>()
        multiValueMap.add("Token1#^", "ad*minis|t#rator")
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("http://localhost:${wiremockPort}/rest-api/some-endpoint".toString())
        httpRequest.setMethod("GET")
        httpRequest.setBody(multiValueMap)
        httpRequest.setHeaders(["Accept": ["application/x-www-url-encoded"]])
        httpRequest.setReadTimeoutSeconds(1)
        httpRequest.setWriteTimeoutSeconds(1)
        httpRequest.setConnectTimeoutSeconds(1)
        httpRequest.setEncodeUrl(true)

        and: "Configure expected rest call from the http executor"
        wireMockServer.stubFor(get(urlEqualTo("/rest-api/some-endpoint"))
                .withHeader("Accept", matching("application/x-www-url-encoded"))
                .withRequestBody(equalTo("Token1%23%5E=ad*minis%7Ct%23rator"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("some response")));

        when: "Execute GET http request"
        ResponseEntity response = httpExecutor.execute(httpRequest)

        then: "Response as expected"
        assert new String(response.body) == "some response"
        assert response.statusCode == HttpStatus.valueOf(200)
    }

    def "Should throw WebClientRequestException when executing get request to unreachable IP"() {
        setup: "Create request properties"
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("http://1.2.3.4:8080/rest-api/some-endpoint".toString())
        httpRequest.setMethod("GET")
        httpRequest.setBody(null)
        httpRequest.setHeaders(["Accept": ["application/json"]])
        httpRequest.setEncodeUrl(true)
        httpRequest.setConnectTimeoutSeconds(1)

        when: "Execute GET http request"
        httpExecutor.execute(httpRequest)

        then: "Exception thrown"
        thrown(WebClientRequestException)
    }

    def "Should throw WebClientRequestException when executing get request to unknown host"() {
        setup: "Create request properties"
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("http://jake.fake:8080/rest-api/some-endpoint".toString())
        httpRequest.setMethod("GET")
        httpRequest.setBody(null)
        httpRequest.setHeaders(["Accept": ["application/json"]])
        httpRequest.setEncodeUrl(true)
        httpRequest.setConnectTimeoutSeconds(1)

        when: "Execute GET http request"
        httpExecutor.execute(httpRequest)

        then: "Exception thrown"
        def exception = thrown(WebClientRequestException)
        assert exception.getRootCause().getClass() in [
            UnknownHostException,
            DnsErrorCauseException,
            DnsNameResolverTimeoutException
        ]
    }

    @TestConfiguration
    @EnableConfigurationProperties
    @ComponentScan(basePackages = ["com.ericsson.bos.dr.rest.service.http", "com.ericsson.bos.dr.rest.service.substitution"])

    static class HttpExecutorTestConfig {

        @Bean
        public WebClient.Builder getWebClientBuilder() {
            return WebClient.builder()
        }
    }
}
