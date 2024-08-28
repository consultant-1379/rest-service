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
package com.ericsson.bos.dr.rest.tests.integration.security

import com.ericsson.bos.dr.rest.security.CertificateEventChangeDetector
import com.ericsson.bos.dr.rest.tests.integration.BaseSpec
import com.ericsson.bos.dr.rest.tests.integration.config.KubernetesCertificateConfiguration
import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.api.model.SecretBuilder
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.Resource
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

class CertificateHandlingSpec extends BaseSpec {

    @Autowired
    CertificateEventChangeDetector certificateEventChangeDetector

    @Autowired
    KubernetesCertificateConfiguration certsConfig

    MixedOperation<Secret, ?, Resource<Secret>> secretsOperation = Mock(MixedOperation<Secret, ?, Resource<Secret>>)

    def setup() {
        certsConfig.getKubernetesClient().secrets() >> secretsOperation
    }

    def "Added or modified k8s secret with correct naming convention is added to TrustManager cache"() {

        setup: "initialize secret with CA cert data"
        byte[] certs = this.getClass().getResource('/security/certs/ca.p12').bytes

        Map secretData = ["server.p12": Base64.encoder.encodeToString(certs)]
        Secret secret = new SecretBuilder().addToData(secretData).withNewMetadata()
                .withName(secretName).withNamespace("test").endMetadata().build();

        and: "set up kubernetesClient watcher to receive ADD event"
        certsConfig.getKubernetesClient().secrets().watch(_ as Watcher) >> { Watcher watcher ->
            watcher.eventReceived(Watcher.Action.ADDED, secret)
        }

        when: "calling subscribe"
        certificateEventChangeDetector.subscribe()

        then: "call made to addSecret"
        invocationNum * externalSslCtx.addTruststoreCert(secretName, _)

        where:
        secretName                                                |  invocationNum
        "eric-esoa-rest-service-truststore-secret"                |  1
        "eric-esoa-rest-service-truststore-secret-from-subsystem" |  1
        "notInScopeSecretName"                                    |  0
    }

    @Unroll
    def "Existing k8s secret is deleted from TrustManager cache"() {

        setup: "initialize secret with CA cert data"
        byte[] certs = this.getClass().getResource('/security/certs/ca.p12').bytes

        Map secretData = ["server.p12": Base64.encoder.encodeToString(certs)]
        Secret secret = new SecretBuilder().addToData(secretData).withNewMetadata()
                .withName(secretName).withNamespace("test").endMetadata().build();

        and: "set up kubernetesClient watcher to receive DELETE event"
        certsConfig.getKubernetesClient().secrets().watch(_ as Watcher) >> { Watcher watcher ->
            watcher.eventReceived(Watcher.Action.DELETED, secret)
        }

        when: "calling subscribe"
        certificateEventChangeDetector.subscribe()

        then: "call made to deleteSecre"
        invocationNum * externalSslCtx.deleteTruststoreCert(secretName)

        where:
        secretName                                                |  invocationNum
        "eric-esoa-rest-service-truststore-secret"                |  1
        "eric-esoa-rest-service-truststore-secret-from-subsystem" |  1
        "notInScopeSecretName"                                    |  0
    }

    @Unroll
    def "Added or modified k8s secret with correct naming convention is added to KeyManager cache"() {

        setup: "initialize secret with client cert data"
        byte[] certs = this.getClass().getResource('/security/certs/client.p12').bytes

        Map secretData = ["client.p12": Base64.encoder.encodeToString(certs)]
        Secret secret = new SecretBuilder().addToData(secretData).withNewMetadata()
                .withName(secretName).withNamespace("test").endMetadata().build();

        and: "set up kubernetesClient watcher to receive ADD event"
        certsConfig.getKubernetesClient().secrets().watch(_ as Watcher) >> { Watcher watcher ->
            watcher.eventReceived(Watcher.Action.ADDED, secret)
        }

        when: "calling subscribe"
        certificateEventChangeDetector.subscribe()

        then: "call made to addSecret"
        invocationNum * externalSslCtx.addKeystoreCert(secretName, _)

        where:
        secretName                                              |  invocationNum
        "eric-esoa-rest-service-keystore-secret"                |  1
        "eric-esoa-rest-service-keystore-secret-from-subsystem" |  1
        "notInScopeSecretName"                                  |  0
    }

    @Unroll
    def "Existing k8s secret is deleted from KeyManager cache"() {

        setup: "initialize secret with client cert data"
        byte[] certs = this.getClass().getResource('/security/certs/client.p12').bytes

        Map<String, String> secretData = ["client.p12": Base64.encoder.encodeToString(certs)]
        Secret secret = new SecretBuilder().addToData(secretData).withNewMetadata()
                .withName(secretName).withNamespace("test").endMetadata().build();

        and: "set up kubernetesClient watcher to receive DELETE event"
        certsConfig.getKubernetesClient().secrets().watch(_ as Watcher) >> { Watcher watcher ->
            watcher.eventReceived(Watcher.Action.DELETED, secret)
        }

        when: "calling subscribe"
        certificateEventChangeDetector.subscribe()

        then: "call made to deleteSecretToCache"
        invocationNum * externalSslCtx.deleteKeystoreCert(secretName)

        where:
        secretName                                              |  invocationNum
        "eric-esoa-rest-service-keystore-secret"                |  1
        "eric-esoa-rest-service-keystore-secret-from-subsystem" |  1
        "notInScopeSecretName"                                  |  0
    }
}
