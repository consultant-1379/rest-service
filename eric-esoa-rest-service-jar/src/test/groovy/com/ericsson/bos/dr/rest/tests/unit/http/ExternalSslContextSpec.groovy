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


import com.ericsson.bos.dr.rest.service.exceptions.ErrorCode
import com.ericsson.bos.dr.rest.service.exceptions.RestServiceException
import com.ericsson.bos.dr.rest.service.http.ExternalSslCtx
import com.ericsson.bos.dr.rest.service.http.HttpRequest
import com.ericsson.bos.dr.rest.tests.integration.utils.IOUtils
import io.netty.handler.ssl.SslContext
import spock.lang.Specification

class ExternalSslContextSpec extends Specification {

    ExternalSslCtx externalSslCtx = new ExternalSslCtx()

    def setup() {
        //Add keystore and truststore secrets
        externalSslCtx.addKeystoreCert("keySecret", IOUtils.readClasspathResourceAsBytes('/security/certs/server.p12'))
        externalSslCtx.addTruststoreCert("trustSecret", IOUtils.readClasspathResourceAsBytes('/security/certs/ca.p12'))
    }

    def "Same SslContext instance is returned for same keystore and truststore in http request"() {

        when: "Get SslContext"
        HttpRequest httpRequest = new HttpRequest(keyStoreSecretName: keyStoreSecretName, trustStoreSecretName: truststoreSecretName,
                trustStoreSecretPassword: "password", keyStoreSecretPassword: "password")
        SslContext sslContext1 = externalSslCtx.get(httpRequest)

        then: "SslContext is returned"
        sslContext1 != null

        when: "Get SslContext again"
        SslContext sslContext2 = externalSslCtx.get(httpRequest)

        then : "Same SSlContext is returned"
        sslContext2 == sslContext1

        where:
        keyStoreSecretName | truststoreSecretName
        "keySecret"        | "trustSecret"
        "keySecret"        | null
        null               | "trustSecret"
    }

    def "New SslContext instance is returned for different keystore and truststore in http request"() {

        when: "Get SslContext"
        HttpRequest httpRequest = new HttpRequest(keyStoreSecretName: "keySecret", trustStoreSecretName: "trustSecret",
                trustStoreSecretPassword: "password", keyStoreSecretPassword: "password")
        SslContext sslContext1 = externalSslCtx.get(httpRequest)

        then: "SslContext is returned"
        sslContext1 != null

        when: "Get SslContext for different truststore"
        externalSslCtx.addTruststoreCert("trustSecret2", IOUtils.readClasspathResourceAsBytes('/security/certs/ca.p12'))
        HttpRequest httpRequest2 = new HttpRequest(keyStoreSecretName: "keySecret", trustStoreSecretName: "trustSecret2",
                trustStoreSecretPassword: "password", keyStoreSecretPassword: "password")
        SslContext sslContext2 = externalSslCtx.get(httpRequest2)

        then : "Different SSlContext is returned"
        sslContext2 != sslContext1

        when: "Get SslContext for different keystore"
        externalSslCtx.addKeystoreCert("keySecret2", IOUtils.readClasspathResourceAsBytes('/security/certs/server.p12'))
        HttpRequest httpRequest3 = new HttpRequest(keyStoreSecretName: "keySecret2", trustStoreSecretName: "trustSecret",
                trustStoreSecretPassword: "password", keyStoreSecretPassword: "password")
        SslContext sslContext3 = externalSslCtx.get(httpRequest3)

        then : "Different SSlContext is returned"
        sslContext3 != sslContext1
        sslContext3 != sslContext2
    }

    def "SslContext is also deleted when the keystore cert is deleted"() {

        when: "Get SslContext"
        HttpRequest httpRequest = new HttpRequest(keyStoreSecretName: "keySecret", trustStoreSecretName: "trustSecret",
                trustStoreSecretPassword: "password", keyStoreSecretPassword: "password")
        SslContext sslContext1 = externalSslCtx.get(httpRequest)

        then: "SslContext is returned"
        sslContext1 != null

        when: "Delete the keystore cert"
        externalSslCtx.deleteKeystoreCert("keySecret")

        and: "Get SslContext again"
        externalSslCtx.get(httpRequest)

        then : "RestServiceException is thrown"
        RestServiceException e = thrown(RestServiceException)
        e.errorMessage.errorCode == ErrorCode.CERTIFICATE_HANDLING_FAILED.errorCode
    }

    def "SslContext is also deleted when  the truststore cert is deleted"() {

        when: "Get SslContext"
        HttpRequest httpRequest = new HttpRequest(keyStoreSecretName: "keySecret", trustStoreSecretName: "trustSecret",
                trustStoreSecretPassword: "password", keyStoreSecretPassword: "password")
        SslContext sslContext1 = externalSslCtx.get(httpRequest)

        then: "SslContext is returned"
        sslContext1 != null

        when: "Delete the truststore cert"
        externalSslCtx.deleteTruststoreCert("trustSecret")

        and: "Get SslContext again"
        externalSslCtx.get(httpRequest)

        then : "RestServiceException is thrown"
        RestServiceException e = thrown(RestServiceException)
        e.errorMessage.errorCode == ErrorCode.CERTIFICATE_HANDLING_FAILED.errorCode
    }

    def "Exception is throw if keystore cert is invalid"() {

        setup: "Add empty keystore cert"
        externalSslCtx.addKeystoreCert("keySecret2", new byte[0])

        when: "Get SslContext"
        HttpRequest httpRequest = new HttpRequest(keyStoreSecretName: "keySecret2", keyStoreSecretPassword: "password")
        externalSslCtx.get(httpRequest)

        then : "RestServiceException is thrown"
        RestServiceException e = thrown(RestServiceException)
        e.errorMessage.errorCode == ErrorCode.CERTIFICATE_HANDLING_FAILED.errorCode
    }

    def "Exception is throw if truststore cert is invalid"() {

        setup: "Add empty truststore cert"
        externalSslCtx.addTruststoreCert("trustSecret2", new byte[0])

        when: "Get SslContext"
        HttpRequest httpRequest = new HttpRequest(trustStoreSecretName: "trustSecret2", trustStoreSecretPassword: "password")
        externalSslCtx.get(httpRequest)

        then : "RestServiceException is thrown"
        RestServiceException e = thrown(RestServiceException)
        e.errorMessage.errorCode == ErrorCode.CERTIFICATE_HANDLING_FAILED.errorCode
    }

}