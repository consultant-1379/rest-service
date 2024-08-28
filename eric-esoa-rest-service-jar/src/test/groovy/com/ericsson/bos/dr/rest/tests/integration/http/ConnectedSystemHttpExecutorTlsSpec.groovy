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

import com.ericsson.bos.dr.rest.service.http.HttpExecutor
import com.ericsson.bos.dr.rest.service.http.HttpRequest
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.Options
import com.google.common.io.Resources
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClientRequestException

import javax.net.ssl.SSLHandshakeException

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.matching
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class ConnectedSystemHttpExecutorTlsSpec extends BaseSpec {

    private static final String KEYSTORE_SECRET_NAME = "keystore_secret"
    private static final String TRUSTSTORE_SECRET_NAME = "truststore_secret"

    private static final WireMockServer secureWireMockServer = new WireMockServer(options()
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
    @Qualifier("connected_system")
    private HttpExecutor httpExecutor

    def setupSpec() {
        secureWireMockServer.start()
        wiremockPort = secureWireMockServer.httpsPort()
    }

    def cleanupSpec() {
        secureWireMockServer.stop()
    }

    def cleanup() {
        secureWireMockServer.resetAll()
    }

    def "Execute http request is successful with MTLS configured using user secrets"() {

        setup: "Create http request properties, configured to use user secrets"
        HttpRequest httpRequest  = createUserSecretHttpRequest(true)

        and: "configure key and trust certs"
        byte[] clientCert = this.getClass().getResource('/security/certs/client.p12').bytes
        externalSslCtx.addKeystoreCert(KEYSTORE_SECRET_NAME, clientCert)
        byte[] trustCert = this.getClass().getResource('/security/certs/ca.p12').bytes
        externalSslCtx.addTruststoreCert(TRUSTSTORE_SECRET_NAME, trustCert)

        and: "Configure expected rest call from the http executor"
        secureWireMockServer.stubFor(get(urlPathEqualTo("/rest-api/some-endpoint"))
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

    def "SSLHandshakeException throw when MTLS configured using user secrets and no client cert"() {

        setup: "Create http request properties without keystore secret"
        HttpRequest httpRequest = createUserSecretHttpRequest(false)

        and: "configure trust certs"
        byte[] trustCert = this.getClass().getResource('/security/certs/ca.p12').bytes
        externalSslCtx.addTruststoreCert(TRUSTSTORE_SECRET_NAME, trustCert)

        when: "Execute GET http request"
        httpExecutor.execute(httpRequest)

        then: "SSLHandshakeException thrown"
        WebClientRequestException exception = thrown(WebClientRequestException)
        exception.getRootCause() instanceof SSLHandshakeException
    }

    def "Execute http request is successful when ssl verify not required"() {

        setup: "Create http request properties with ssl verify not required"
        HttpRequest httpRequest  = createUnsecureHttpRequest()

        and: "Configure expected rest call from the http executor"
        wireMock.stubFor(get(urlPathEqualTo("/rest-api/some-endpoint"))
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

    def "Execute http request is successful with MTLS configured using SIP TLS"() {

        setup: "Create http request properties, configured to use SIP TLS"
        HttpRequest httpRequest = createSipTlsHttpRequest()

        and: "setup keystore and truststore"
        setupTrustStoreForsipTls("security/certs/truststore.jks")
        setupKeyStoreForSipTls("security/certs/keystore.jks")

        and: "Configure expected rest call from the http executor"
        secureWireMockServer.stubFor(get(urlPathEqualTo("/rest-api/some-endpoint"))
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

    HttpRequest createUserSecretHttpRequest(boolean setKeystoreSecret) {
        HttpRequest httpRequest = createBaseHttpRequest("https", wiremockPort, true)
        if (setKeystoreSecret) {
            httpRequest.setKeyStoreSecretName(KEYSTORE_SECRET_NAME)
            httpRequest.setKeyStoreSecretPassword("password")
        }
        httpRequest.setTrustStoreSecretName(TRUSTSTORE_SECRET_NAME)
        httpRequest.setTrustStoreSecretPassword("password")
        return httpRequest
    }

    HttpRequest createUnsecureHttpRequest() {
        return createBaseHttpRequest("http", 8081, false)
    }

    HttpRequest createSipTlsHttpRequest() {
        HttpRequest httpRequest = createBaseHttpRequest("https", wiremockPort, true)
        httpRequest.setTrustStoreSecretName("internal")
        return httpRequest
    }

    HttpRequest createBaseHttpRequest(String protocol, int port, boolean sslVerify) {
        HttpRequest httpRequest = new HttpRequest()
        httpRequest.setUrl("${protocol}://localhost:${port}/rest-api/some-endpoint".toString())
        httpRequest.setMethod("GET")
        httpRequest.setBody(null)
        httpRequest.setHeaders(["Accept": ["application/json"]])
        httpRequest.setReadTimeoutSeconds(1)
        httpRequest.setWriteTimeoutSeconds(1)
        httpRequest.setConnectTimeoutSeconds(1)
        httpRequest.setEncodeUrl(true)
        httpRequest.setSslVerify(sslVerify)
        return httpRequest
    }

    def setupKeyStoreForSipTls(String keyStoreResource) {
        Path keyStorePath = Paths.get((new File(Resources.getResource(keyStoreResource).getPath())).getAbsolutePath())
        Files.copy(keyStorePath, Paths.get(System.getProperty("java.io.tmpdir"),"keystore.jks"), StandardCopyOption.REPLACE_EXISTING)
    }

    def setupTrustStoreForsipTls(String trustStoreResource) {
        Path trustStorePath = Paths.get((new File(Resources.getResource(trustStoreResource).getPath())).getAbsolutePath())
        Files.copy(trustStorePath, Paths.get(System.getProperty("java.io.tmpdir"),"truststore.jks"), StandardCopyOption.REPLACE_EXISTING)
    }
}
