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

import com.ericsson.bos.dr.rest.service.http.ExternalSslCtx
import com.ericsson.bos.dr.rest.service.http.HttpExecutor
import org.spockframework.spring.SpringSpy

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.ericsson.bos.dr.rest.tests.integration.config.KubernetesCertificateConfiguration
import com.ericsson.bos.dr.rest.tests.integration.config.TestContainerConfiguration
import com.ericsson.bos.dr.rest.tests.integration.config.WebClientConfiguration
import com.ericsson.bos.dr.rest.RestServiceApplication
import com.ericsson.bos.dr.rest.tests.integration.config.PostgresDatabaseConfiguration
import com.ericsson.bos.dr.rest.tests.integration.teststeps.ResourceConfigurationTestSteps
import com.ericsson.bos.dr.rest.tests.integration.teststeps.RunServiceTestSteps
import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.cache.CacheManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(classes = [TestContainerConfiguration.class, PostgresDatabaseConfiguration.class, RestServiceApplication.class,
        KubernetesCertificateConfiguration.class, WebClientConfiguration.class])
@AutoConfigureMockMvc
@Sql("classpath:sql/clean.sql")
abstract class BaseSpec extends Specification {

    @Autowired
    ResourceConfigurationTestSteps resourceConfigurationTestSteps

    @Autowired
    RunServiceTestSteps runServiceTestSteps

    @Autowired
    CacheManager cacheManager

    @Autowired
    WebClientConfiguration.WebClientRequestsRecorder webClientRequestsRecorder

    @SpringSpy
    ExternalSslCtx externalSslCtx

    @SpringSpy(name="connected_system")
    HttpExecutor httpExecutor

    @Shared
    static WireMockServer wireMock = new WireMockServer(options().port(8081))

    def setupSpec() {
        wireMock.start()
        configureFor(wireMock.port())
    }

    def cleanupSpec() {
        wireMock.stop()
    }

    def cleanup() {
        wireMock.resetAll()
        cacheManager.getCache("subsystem_cache").clear()
        cacheManager.getCache("auth_token_cache").clear()
        webClientRequestsRecorder.clear()
    }
}