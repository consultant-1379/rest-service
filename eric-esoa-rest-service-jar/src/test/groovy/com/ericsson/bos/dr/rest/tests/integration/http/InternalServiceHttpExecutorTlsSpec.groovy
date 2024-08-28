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
package com.ericsson.bos.dr.rest.tests.integration.http

import com.ericsson.bos.so.security.mtls.MtlsConfigurationReloadersRegister
import org.springframework.boot.context.properties.EnableConfigurationProperties

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.matching
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.ericsson.bos.dr.rest.service.http.ConnectedSystemHttpExecutor
import com.ericsson.bos.dr.rest.service.http.HttpExecutor
import com.ericsson.bos.dr.rest.service.http.HttpRequest
import com.ericsson.bos.dr.rest.service.http.ExternalSslCtx
import org.springframework.context.annotation.FilterType
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.Options
import com.google.common.io.Resources
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
import spock.lang.Specification

import java.nio.file.Files

import javax.net.ssl.SSLHandshakeException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * NOTE: The keystore.jks and truststore.jks for this test where populated with the client.p12 and ca.p12 files in the test resources.
 *
 * <p>
 *     keytool -importkeystore -srckeystore client.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS -srcstorepass password -deststorepass password -destkeypass password -noprompt
 * </p>
 * <p>
 *     keytool -importkeystore -srckeystore ca.p12 -srcstoretype PKCS12 -destkeystore truststore.jks -deststoretype JKS -srcstorepass password -deststorepass password -destkeypass password -noprompt
 * </p>
 *
 */
@ContextConfiguration(classes = InternalHttpExecutorTestConfig.class)
@TestPropertySource(properties = ["security.tls.enabled=true"])
@TestPropertySource(locations="classpath:application.properties")
class InternalServiceHttpExecutorTlsSpec extends Specification {

    private static final WireMockServer wireMockServer = new WireMockServer(options()
            .dynamicHttpsPort().preserveHostHeader(true)
            .needClientAuth(true)
            .trustStoreType("PKCS12")
            .trustStorePath((new File(Resources.getResource("security/certs/ca.p12").getPath())).getAbsolutePath())
            .trustStorePassword("password")
            .keystoreType("PKCS12")
            .keystorePath((new File(Resources.getResource("security/certs/server.p12").getPath())).getAbsolutePath())
            .keystorePassword("password")
            .keyManagerPassword("password")
            .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.BODY_FILE)
            .gzipDisabled(true))

    private static int wiremockPort

    @Autowired
    @Qualifier("internal_service")
    private HttpExecutor httpExecutor

    def setupSpec() {
        wireMockServer.start()
        wiremockPort = wireMockServer.httpsPort()
        setupTrustStore("security/certs/truststore.jks")
        setupKeyStore("security/certs/keystore.jks")
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def cleanup() {
        wireMockServer.resetAll()
    }

    def "Execute http request is successful with MTLS configured"() {

        setup: "Create http request properties"
        HttpRequest httpRequest = createHttpRequest()

        and: "Reload sslContext"
        MtlsConfigurationReloadersRegister.getInstance().getMtlsConfigurationReloaders().forEach({ it.reload() })

        and: "Configure expected rest call from the http executor"
        wireMockServer.stubFor(get(urlPathEqualTo("/rest-api/some-endpoint"))
                .withHeader("Accept", matching("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("some response")));

        when: "Execute GET http request"
        ResponseEntity response = httpExecutor.execute(httpRequest)

        then: "Response as expected"
        assert new String(response.body) == "some response"
        assert response.statusCode == HttpStatus.valueOf(200)
    }

    def "SSLHandshakeException thrown when MTLS configured and invalid client key"() {

        setup: "Create http request properties"
        HttpRequest httpRequest = createHttpRequest()

        and: "Setup keystore with invalid client key"
        setupKeyStore("security/certs/invalid_keystore.jks")

        and: "Reload sslContext"
        MtlsConfigurationReloadersRegister.getInstance().getMtlsConfigurationReloaders().forEach({ it.reload() })

        when: "Execute GET http request"
        httpExecutor.execute(httpRequest)

        then: "SSLHandshakeException thrown"
        WebClientRequestException exception = thrown(WebClientRequestException)
        exception.getRootCause() instanceof SSLHandshakeException
    }

    def setupKeyStore(String keyStoreResource) {
        Path keyStorePath = Paths.get((new File(Resources.getResource(keyStoreResource).getPath())).getAbsolutePath())
        Files.copy(keyStorePath, Paths.get(System.getProperty("java.io.tmpdir"),"keystore.jks"), StandardCopyOption.REPLACE_EXISTING)
    }

    def setupTrustStore(String trustStoreResource) {
        Path trustStorePath = Paths.get((new File(Resources.getResource(trustStoreResource).getPath())).getAbsolutePath())
        Files.copy(trustStorePath, Paths.get(System.getProperty("java.io.tmpdir"),"truststore.jks"), StandardCopyOption.REPLACE_EXISTING)
    }

    HttpRequest createHttpRequest() {
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("https://localhost:${wiremockPort}/rest-api/some-endpoint".toString())
        httpRequest.setMethod("GET")
        httpRequest.setBody(null)
        httpRequest.setHeaders(["Accept": ["application/json"]])
        httpRequest.setReadTimeoutSeconds(1)
        httpRequest.setWriteTimeoutSeconds(1)
        httpRequest.setConnectTimeoutSeconds(1)
        httpRequest.setEncodeUrl(true)
        return httpRequest
    }


    @TestConfiguration
    @EnableConfigurationProperties
    @ComponentScan(basePackages = ["com.ericsson.bos.dr.rest.service.http"],
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [ConnectedSystemHttpExecutor.class,
                    ExternalSslCtx.class]))
    static class InternalHttpExecutorTestConfig {

        @Bean
        public WebClient.Builder getWebClientBuilder() {
            return WebClient.builder();
        }
    }
}
